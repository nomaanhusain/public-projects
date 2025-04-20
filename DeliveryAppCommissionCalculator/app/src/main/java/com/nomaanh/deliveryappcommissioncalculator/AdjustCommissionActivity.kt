package com.nomaanh.deliveryappcommissioncalculator

import android.app.Activity
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.nomaanh.deliveryappcommissioncalculator.databinding.ActivityAdjustCommissionBinding

class AdjustCommissionActivity : AppCompatActivity() {
    private var commission:Float?=null
    private var paymentGatewayCharge:Float?=null
    private var gst:Float?=null
    private var binding: ActivityAdjustCommissionBinding?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityAdjustCommissionBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        //For commission
        val commsn: SharedPreferences = getSharedPreferences("commission", MODE_PRIVATE)
        commission = commsn.getFloat("valueComm", 22.0f)

        //For PGC
        val pgcPref: SharedPreferences = getSharedPreferences("paymentGatewayCharge", MODE_PRIVATE)
        paymentGatewayCharge = pgcPref.getFloat("valuePgc", 1.84f)

        //For GST
        val gstPref: SharedPreferences = getSharedPreferences("gst", MODE_PRIVATE)
        gst = gstPref.getFloat("valueGst", 18.0f)

        binding?.tvCommission?.text="The commission is $commission%, PGC is $paymentGatewayCharge% and GST is $gst%"

        binding?.btnSet?.setOnClickListener {
            val editorCommission: SharedPreferences.Editor=commsn.edit()
            val editorPGC: SharedPreferences.Editor=pgcPref.edit()
            val editorGST: SharedPreferences.Editor=gstPref.edit()
            if(binding?.etCommission?.text.toString().isEmpty() ||
                binding?.etPGC?.text.toString().isEmpty() ||
                binding?.etGST?.text.toString().isEmpty()){
                Toast.makeText(this,
                    "Some Text field is empty",
                    Toast.LENGTH_SHORT).show()
            }else{
                commission=(binding?.etCommission?.text.toString()).toFloat()

                gst=(binding?.etGST?.text.toString()).toFloat()
                paymentGatewayCharge=(binding?.etPGC?.text.toString()).toFloat()
                editorCommission.putFloat("valueComm",commission!!)
                editorPGC.putFloat("valuePgc", paymentGatewayCharge!!)
                editorGST.putFloat("valueGst", gst!!)

                editorCommission.apply()
                editorPGC.apply()
                editorGST.apply()
                setResult(Activity.RESULT_OK)
                binding?.tvCommission?.text="The commission is $commission%, PGC is $paymentGatewayCharge% and GST is $gst%"
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding=null
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this@AdjustCommissionActivity.finish()
    }


}