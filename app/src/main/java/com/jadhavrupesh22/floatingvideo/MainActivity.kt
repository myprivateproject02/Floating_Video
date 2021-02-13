package com.jadhavrupesh22.floatingvideo

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.jadhavrupesh22.floatingvideo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var alert: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val floatingWindow = FloatingWindow()

        binding.start.setOnClickListener { v ->
            if (checkPermission()) {
                startService(Intent(this@MainActivity, FloatingWindow::class.java))
            } else {
                reqPermission()
            }
        }

        binding.stop.setOnClickListener { v ->
            Toast.makeText(this, "Stop Clicked", Toast.LENGTH_SHORT).show()
            stopService(Intent(this@MainActivity, FloatingWindow::class.java))
        }
    }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                reqPermission()
                false
            } else {
                true
            }
        } else {
            true
        }
    }

    private fun reqPermission() {
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setCancelable(true)
        alertBuilder.setTitle("Screen overlay detected")
        alertBuilder.setMessage("Enable 'Draw over other apps' in your system setting.")
        alertBuilder.setPositiveButton(
            "OPEN SETTINGS"
        ) { dialog, which ->
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, RESULT_OK)
        }
        alert = alertBuilder.create()
        alert.show()
    }


    override fun onPause() {
        super.onPause()
        stopService(Intent(this@MainActivity, FloatingWindow::class.java))
    }

    override fun onStop() {
        super.onStop()
        stopService(Intent(this@MainActivity, FloatingWindow::class.java))
    }


}