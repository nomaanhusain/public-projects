package com.example.publictransporttimer

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.publictransporttimer.journey.Journey1
import com.example.publictransporttimer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val locationPref: SharedPreferences = getSharedPreferences("locationDetails", MODE_PRIVATE)

        if (locationPref.getBoolean("locationStored",false)){
            startActivity(Intent(this,Journey1::class.java))
            finish()
        }else{
            startActivity(Intent(this,OrgSettings::class.java))
            finish()
        }

        binding?.btJourney1?.setOnClickListener {
            binding?.progressBar1?.visibility = View.VISIBLE
            val intent = Intent(this, Journey1::class.java)
            startActivity(intent)
        }

        binding?.btnSettings?.setOnClickListener {
            val intent = Intent(this,OrgSettings::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        binding?.progressBar1?.visibility = View.GONE
    }
}

