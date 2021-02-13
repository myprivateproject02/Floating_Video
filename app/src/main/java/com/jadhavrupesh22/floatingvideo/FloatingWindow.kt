package com.jadhavrupesh22.floatingvideo

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.view.*
import android.view.View.OnTouchListener
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory

class FloatingWindow : Service() {

    lateinit var wm: WindowManager

    private var videoPlayer: SimpleExoPlayer? = null
    private var sampleUrl =
        "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4"

    private lateinit var mFloatingView: View

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null)
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val LAYOUT_FLAG: Int
        LAYOUT_FLAG = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        //Add the view to the window.
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            LAYOUT_FLAG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        //Specify the view position
        params.gravity =
            Gravity.TOP or Gravity.CENTER //Initially view will be added to top-left corner
        params.x = 0
        params.y = 0

        //Add the view to the window
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        wm.addView(mFloatingView, params)
        mFloatingView.findViewById<View>(R.id.video_player_view)
            .setOnTouchListener(object : OnTouchListener {

                private var initialX = 0
                private var initialY = 0
                private var initialTouchX = 0f
                private var initialTouchY = 0f
                override fun onTouch(v: View, event: MotionEvent): Boolean {

                    mFloatingView.findViewById<PlayerView>(R.id.video_player_view)?.showController()
                    mFloatingView.findViewById<PlayerView>(R.id.video_player_view)?.controllerShowTimeoutMs =
                        2000

                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {

                            //remember the initial position.
                            initialX = params.x
                            initialY = params.y

                            //get the touch location
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            return true
                        }
                        MotionEvent.ACTION_UP -> {
                            val Xdiff = (event.rawX - initialTouchX).toInt()
                            val Ydiff = (event.rawY - initialTouchY).toInt()

                            //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                            //So that is click event.
                            if (Xdiff < 10 && Ydiff < 10) {
                            }
                            return true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            //Calculate the X and Y coordinates of the view.
                            params.x = initialX + (event.rawX - initialTouchX).toInt()
                            params.y = initialY + (event.rawY - initialTouchY).toInt()

                            //Update the layout with new X & Y coordinate
                            wm!!.updateViewLayout(mFloatingView, params)
                            return true
                        }
                    }
                    return false
                }
            })
        initializePlayer()

        mFloatingView.setOnClickListener(View.OnClickListener { view: View? ->
            val home = Intent(this@FloatingWindow, MainActivity::class.java)
            home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(home)
        })

        mFloatingView.findViewById<View>(R.id.close_btn).setOnClickListener { v: View? ->
            stopSelf()
            wm.removeView(mFloatingView)
        }


    }

    private fun buildMediaSource(): MediaSource? {
        val dataSourceFactory = DefaultDataSourceFactory(this, "sample")
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(Uri.parse(sampleUrl))
    }

    private fun initializePlayer() {
        videoPlayer = SimpleExoPlayer.Builder(this).build()
        mFloatingView.findViewById<PlayerView>(R.id.video_player_view)?.player = videoPlayer
        buildMediaSource()?.let {
            videoPlayer?.prepare(it)
        }
        videoPlayer?.playWhenReady = true

        mFloatingView.findViewById<PlayerView>(R.id.video_player_view)?.keepScreenOn = true


    }

    private fun releasePlayer() {
        videoPlayer?.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSelf()
        wm.removeView(mFloatingView)
        videoPlayer?.playWhenReady = false
        releasePlayer()

    }

}