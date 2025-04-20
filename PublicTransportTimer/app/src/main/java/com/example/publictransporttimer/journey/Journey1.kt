package com.example.publictransporttimer.journey

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.publictransporttimer.OrgSettings
import com.example.publictransporttimer.databinding.ActivityJourney1Binding

class Journey1 : AppCompatActivity() {
    private var binding:ActivityJourney1Binding?=null
    private var weHaveEverything:Boolean = false
    private var originText:String = "[_]"
    private var destText:String = "[_]"

    //TODO results are opposite, fix it
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJourney1Binding.inflate(layoutInflater)
        setContentView(binding?.root)

        val locationPref: SharedPreferences = getSharedPreferences("locationDetails", MODE_PRIVATE)

        weHaveEverything = locationPref.getBoolean("locationStored",false)


        if (weHaveEverything) {
            originText = locationPref.getString("originName","[_]")!!
            destText = locationPref.getString("dstName","[_]")!!
            doTheMagic()
        }else{
            Toast.makeText(this,"Origin or Destination not found or not setup",Toast.LENGTH_SHORT).show()
        }


        binding?.fabRefreshButton?.setOnClickListener {
            Toast.makeText(this,"Refreshing..", Toast.LENGTH_SHORT).show()
            if (weHaveEverything) {
                doTheMagic()
            }else{
                Toast.makeText(this,"Origin or Destination not found or not setup",Toast.LENGTH_SHORT).show()
            }
        }

        binding?.btnSettingsJourney?.setOnClickListener {
            val intent = Intent(this,OrgSettings::class.java)
            startActivity(intent)
        }
    }
    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
    }

    private fun doTheMagic(){
        Thread{
            var timeForNextDeptToUniUnformatted = executePythonScriptToUni()
            var timeForNextDepartureToHomeUnformatted = executePythonScriptToHome()

            runOnUiThread {
                binding?.toHomeText?.text = "From $destText to $originText"
                binding?.toUniText?.text = "From $originText to $destText"

                if (timeForNextDeptToUniUnformatted == "Network Error" || timeForNextDepartureToHomeUnformatted == "Network Error"){
                        Toast.makeText(this,"No Network or Invalid stop codes", Toast.LENGTH_LONG).show()
                        binding?.tvToUni?.text = "No Network"
                        binding?.tvToHome?.text = "No Network"
                }else{
                    val timeForNextDeptToUni = formatOutput(timeForNextDeptToUniUnformatted)
                    val timeForNextDepartureToHome = formatOutput(timeForNextDepartureToHomeUnformatted)

                    binding?.tvToUni?.text = "Time Left: ${timeForNextDeptToUni[0]} Mins"
                    binding?.tvToHome?.text = "Time Left: ${timeForNextDepartureToHome[0]} Mins"
                    binding?.tvToUniTime?.text = "Timetabled: ${timeForNextDeptToUni[1]}"
                    binding?.tvToHomeTime?.text = "Timetabled: ${timeForNextDepartureToHome[1]}"

                    //Test
                    binding?.tvOrgDelay?.text = " + ${timeForNextDeptToUni[2]} min delay"
                    binding?.tvDstDelay?.text = " + ${timeForNextDepartureToHome[2]} min delay"
                    if (timeForNextDeptToUni[2]=="0.00"){
                        binding?.tvOrgDelay?.visibility = View.INVISIBLE
                    }
                    if (timeForNextDepartureToHome[2]=="0.00"){
                        binding?.tvDstDelay?.visibility = View.INVISIBLE
                    }
                }
            }
        }.start()
    }

    private fun formatOutput(input:String):Array<String>{
        var count:String=""
        var time:String=""
        var delay:String=""
        var starTracker:Int = 0
        for (i in input.indices){
            if (input[i]=='*'){
                starTracker+=1
                continue
            }
            if (starTracker == 0){
                count += input[i]
            }
            if (starTracker == 1){
                time += input[i]
            }
            if (starTracker == 2){
                delay += input[i]
            }
        }
        return arrayOf(count,time,delay)
    }
    private fun executePythonScriptToUni(): String {
        return try {
            if (!Python.isStarted()){
                Python.start(AndroidPlatform(this))
            }
            val python = Python.getInstance()
            val pythonFile = python.getModule("scriptToUni")

            val scriptResult = pythonFile.callAttr("letsDoIt")

            scriptResult.toString()
        }catch (exp:Exception){
            exp.printStackTrace()
            "Network Error"
        }
    }
    private fun executePythonScriptToHome(): String {
        return try {
            if (!Python.isStarted()){
                Python.start(AndroidPlatform(this))
            }
            val python = Python.getInstance()
            val pythonFile = python.getModule("scriptToHome")

            val scriptResult = pythonFile.callAttr("letsDoIt")

            scriptResult.toString()

        }catch (exp:Exception){
            exp.printStackTrace()
            "Network Error"
        }

    }
}