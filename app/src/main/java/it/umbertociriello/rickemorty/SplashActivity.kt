package it.umbertociriello.rickemorty

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import it.umbertociriello.rickemorty.databinding.ActivitySplashBinding
import org.jetbrains.anko.startActivity

class SplashActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivitySplashBinding>(
            this,
            R.layout.activity_splash
        )
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
            startActivity<MainActivity>()
        }, 2000)

    }
}