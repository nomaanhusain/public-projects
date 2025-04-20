package com.example.projectmanager.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import com.example.projectmanager.databinding.ActivitySplachBinding
import com.example.projectmanager.firebase.FirestoreClass

class SplashActivity : AppCompatActivity() {
    private var binding:ActivitySplachBinding?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=ActivitySplachBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        //To hide status bar on splash screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        //To add custom fonts, first create a assets folder (new->folder->assets) paste font file there then below code
        val typeFace: Typeface = Typeface.createFromAsset(assets,"Uni Sans Heavy.otf")
        binding?.tvTitle?.typeface=typeFace

        Handler(Looper.getMainLooper()).postDelayed({
            val currentUserId=FirestoreClass().getCurrentUserId()
            if(currentUserId.isNotEmpty()){
                startActivity(Intent(this, MainActivity::class.java))
            }else{
                startActivity(Intent(this, IntroActivity::class.java))
            }
            finish()
        },2500)
    }
}