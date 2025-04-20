package com.example.publictransporttimer

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.publictransporttimer.journey.Journey1
import com.example.publictransporttimer.databinding.ActivitySettingsBinding
import com.example.publictransporttimer.databinding.StopExcelInfoDialogBinding

class OrgSettings : AppCompatActivity() {
    private var binding:ActivitySettingsBinding?=null
    private var originFilled:Boolean = false
    private var destinationFilled:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        val locationPref:SharedPreferences = getSharedPreferences("locationDetails", MODE_PRIVATE)

        if (locationPref.getString("originCode",null)!=null && locationPref.getString("dstCode",null)!=null){
            val editorSP:SharedPreferences.Editor = locationPref.edit()
            editorSP.putBoolean("locationStored",true)
            editorSP.apply()
        }

        binding?.btnSaveOrigin?.setOnClickListener {
            if (binding?.etOriginName?.text.toString().isEmpty() || binding?.etOriginCode?.text.toString().isEmpty()){
                Toast.makeText(this,"Please enter something in both text fields",Toast.LENGTH_LONG).show()
            }else{
                val orgName = binding?.etOriginName?.text.toString()
                val orgCode = binding?.etOriginCode?.text.toString()
                originFilled = true
                val editorSp:SharedPreferences.Editor = locationPref.edit()
                editorSp.putString("originName",orgName)
                editorSp.putString("originCode",orgCode)
                editorSp.apply()
                setResult(Activity.RESULT_OK)
                updateOrigin(orgCode)
            }
        }

        binding?.btnSaveDestination?.setOnClickListener {
            if (binding?.etDestinationName?.text.toString().isEmpty() || binding?.etDestiantionCode?.text.toString().isEmpty()){
                Toast.makeText(this,"Please enter something in both text fields",Toast.LENGTH_LONG).show()
            }else{
                val dstName = binding?.etDestinationName?.text.toString()
                val dstCode = binding?.etDestiantionCode?.text.toString()
                destinationFilled = true

                val editorSp:SharedPreferences.Editor = locationPref.edit()
                editorSp.putString("dstName",dstName)
                editorSp.putString("dstCode",dstCode)
                editorSp.apply()
                setResult(Activity.RESULT_OK)
                updateDestination(dstCode)
            }
        }

        binding?.btnDone?.setOnClickListener {
            if (originFilled && destinationFilled){
                val editorSp:SharedPreferences.Editor = locationPref.edit()
                editorSp.putBoolean("locationStored",true)
                editorSp.apply()
                val intent = Intent(this,Journey1::class.java)
                startActivity(intent)
                finish()
            }else{
                Toast.makeText(this,"Destination or Orgin Missing",Toast.LENGTH_SHORT).show()
            }
        }


        binding?.btnIvInfoOrigin?.setOnClickListener {
            showCustomDialog()
        }
        binding?.btnIvInfoDest?.setOnClickListener {
            showCustomDialog()
        }
    }
    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
    }

    private fun updateOrigin(origin: String){
        Thread{
            val resFromScript = executeScriptToUpdateOrigin(origin)

            runOnUiThread {
                if (resFromScript == "Success") Toast.makeText(this,"Update Success",Toast.LENGTH_SHORT).show()
                if (resFromScript == "Write Error") Toast.makeText(this,"Write Error",Toast.LENGTH_SHORT).show()
            }
        }.start()
    }
    private fun executeScriptToUpdateOrigin(origin:String):String{
        return try {
            if (!Python.isStarted()){
                Python.start(AndroidPlatform(this))
            }
            val python = Python.getInstance()
            val pythonFile = python.getModule("scriptToHome")
            val res = pythonFile.callAttr("updateScriptOrigin",origin)
            runOnUiThread {
                println("ORIGIN RES = ${res.toString()}")
            }
            "Success"
        }catch (exp:Exception){
            runOnUiThread {
                exp.printStackTrace()
            }
            "Write Error"
        }
    }
    private fun updateDestination(destination:String){
        Thread{
            val resFromScript = executeScriptToUpdateDestination(destination)

            runOnUiThread {
                if (resFromScript == "Success") Toast.makeText(this,"Update Success",Toast.LENGTH_SHORT).show()
                if (resFromScript == "Write Error") Toast.makeText(this,"Write Error",Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    private fun executeScriptToUpdateDestination(destination: String): String {
        return try {
            if (!Python.isStarted()){
                Python.start(AndroidPlatform(this))
            }
            val python = Python.getInstance()
            val pythonFile = python.getModule("scriptToHome")
            val res = pythonFile.callAttr("updateScriptDestination",destination)
            runOnUiThread {
                println("DESTINATION RES = ${res.toString()}")
            }
            "Success"
        }catch (exp:Exception){
            "Write Error"
        }
    }

    private fun showCustomDialog(){
        val customDialog = Dialog(this)
        val dialogBinding = StopExcelInfoDialogBinding.inflate(layoutInflater)
        customDialog.setContentView(dialogBinding.root)
        customDialog.setCanceledOnTouchOutside(true)
        customDialog.show()
        dialogBinding.btnCancel.setOnClickListener {
            customDialog.dismiss()
        }
        dialogBinding.btnLink.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.nvbw.de/open-data/haltestellen"))
            startActivity(browserIntent)
        }
    }
}