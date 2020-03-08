package com.example.licenta.view.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.example.licenta.R
import kotlin.random.Random


class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        var handler: Handler? = null
        var progressStatus = 0

        handler = Handler(Handler.Callback {
            progressStatus += Random.nextInt(0, 10)
            progressBar.progress = progressStatus
            handler?.sendEmptyMessageDelayed(0, 100)

            true
        })

        handler.postDelayed({
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }, 3000)

        handler.sendEmptyMessage(0)

    }
}