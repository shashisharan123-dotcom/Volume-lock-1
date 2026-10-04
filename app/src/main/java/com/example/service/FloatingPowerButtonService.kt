package com.example.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.R
import com.example.data.VolumeWakePreferences
import kotlin.math.abs

class FloatingPowerButtonService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingRootView: View? = null
    private var params: WindowManager.LayoutParams? = null

    companion object {
        var isRunning = false
            private set

        fun start(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                return
            }
            val intent = Intent(context, FloatingPowerButtonService::class.java)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingPowerButtonService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        isRunning = true
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        initFloatingView()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initFloatingView() {
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val sizeDp = VolumeWakePreferences.floatingButtonSize.value
        val density = resources.displayMetrics.density
        val sizePx = (sizeDp * density).toInt()

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = 350
        }

        val root = FrameLayout(this)
        root.alpha = VolumeWakePreferences.floatingButtonAlpha.value

        // Main circular bubble
        val bubble = ImageView(this).apply {
            setImageResource(R.drawable.ic_floating_power)
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0xEE121824.toInt())
                setStroke((2 * density).toInt(), 0xFF00E5FF.toInt())
            }
            background = bg
            val p = (10 * density).toInt()
            setPadding(p, p, p, p)
            layoutParams = FrameLayout.LayoutParams(sizePx, sizePx)
        }

        // Popup menu panel (hidden by default)
        val menuPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 18 * density
                setColor(0xF0101726.toInt())
                setStroke((1.5f * density).toInt(), 0xFF00E5FF.toInt())
            }
            background = bg
            setPadding((12 * density).toInt(), (10 * density).toInt(), (12 * density).toInt(), (10 * density).toInt())
        }

        // Menu item: Lock Screen
        val lockItem = createMenuItem("🔒 Lock Screen") {
            VolumeWakeAccessibilityService.lockDevice()
            menuPanel.visibility = View.GONE
        }
        // Menu item: Power Menu
        val powerItem = createMenuItem("⚡ Power Menu") {
            VolumeWakeAccessibilityService.showPowerMenu()
            menuPanel.visibility = View.GONE
        }
        // Menu item: Wake Screen
        val wakeItem = createMenuItem("☀️ Wake Screen") {
            VolumeWakeAccessibilityService.wakeScreenExplicitly(this)
            menuPanel.visibility = View.GONE
        }
        // Menu item: Dismiss
        val dismissItem = createMenuItem("✕ Close Menu") {
            menuPanel.visibility = View.GONE
        }

        menuPanel.addView(lockItem)
        menuPanel.addView(powerItem)
        menuPanel.addView(wakeItem)
        menuPanel.addView(dismissItem)

        root.addView(bubble)
        root.addView(menuPanel)

        // Touch listener for dragging bubble and clicking
        bubble.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isMoving = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params!!.x
                        initialY = params!!.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isMoving = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (abs(dx) > 10 || abs(dy) > 10) {
                            isMoving = true
                            params!!.x = initialX + dx
                            params!!.y = initialY + dy
                            windowManager?.updateViewLayout(root, params)
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isMoving) {
                            // Click detected: toggle menu
                            menuPanel.visibility = if (menuPanel.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                        } else {
                            // Snap to nearest side
                            val screenWidth = resources.displayMetrics.widthPixels
                            val targetX = if (params!!.x < screenWidth / 2) 20 else (screenWidth - sizePx - 20)
                            params!!.x = targetX
                            windowManager?.updateViewLayout(root, params)
                        }
                        return true
                    }
                }
                return false
            }
        })

        floatingRootView = root
        try {
            windowManager?.addView(root, params)
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun createMenuItem(text: String, onClick: () -> Unit): TextView {
        val density = resources.displayMetrics.density
        return TextView(this).apply {
            this.text = text
            setTextColor(Color.WHITE)
            textSize = 14f
            setPadding((10 * density).toInt(), (8 * density).toInt(), (10 * density).toInt(), (8 * density).toInt())
            setOnClickListener { onClick() }
        }
    }

    override fun onDestroy() {
        isRunning = false
        floatingRootView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                // View not attached
            }
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
