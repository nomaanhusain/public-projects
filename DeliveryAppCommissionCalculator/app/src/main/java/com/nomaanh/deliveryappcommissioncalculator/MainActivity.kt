package com.nomaanh.deliveryappcommissioncalculator

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import com.nomaanh.deliveryappcommissioncalculator.databinding.ActivityMainBinding
import com.nomaanh.deliveryappcommissioncalculator.databinding.MainPageDialogLayoutBinding

class MainActivity : AppCompatActivity() {
    private var commission:Float?=null
    private var paymentGatewayCharge:Float?=null
    private var gst:Float?=null
    private var binding: ActivityMainBinding?=null

    private var commsn: SharedPreferences?=null
    private var pgcPref: SharedPreferences?=null
    private var gstPref: SharedPreferences?=null


    private val registerOnUpdateCommission = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){result->
        if (result.resultCode==Activity.RESULT_OK){
            updateSharedPrefAndUI()
        }
    }

    //We will share and store values of commission gst and pgc via shared prefrence
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding?.root)
        updateSharedPrefAndUI()

        binding?.ivAdjustRates?.setOnClickListener {
            val intent= Intent(this,AdjustCommissionActivity::class.java)
            registerOnUpdateCommission.launch(intent)
        }
        binding?.tvAdjustRates?.setOnClickListener {
            val intent= Intent(this,AdjustCommissionActivity::class.java)
            startActivity(intent)
        }

        binding?.btnIncrease?.setOnClickListener {
            val intent= Intent(this,IncreaseMoneyActivity::class.java)
            startActivity(intent)
        }

        binding?.btnGetMoney?.setOnClickListener {
            val intent= Intent(this,GetMoneyActivity::class.java)
            startActivity(intent)
        }

        binding?.ivInfoBtn?.setOnClickListener {
            showDialogBox()
        }

    }


    private fun updateSharedPrefAndUI(){
        commsn = getSharedPreferences("commission", MODE_PRIVATE)
        //If value is already present, it will take that or else it will set it to 22.0
        commission = commsn?.getFloat("valueComm", 22.0f)

        pgcPref= getSharedPreferences("paymentGatewayCharge", MODE_PRIVATE)
        paymentGatewayCharge = pgcPref?.getFloat("valuePgc", 1.84f)

        gstPref= getSharedPreferences("gst", MODE_PRIVATE)
        gst = gstPref?.getFloat("valueGst", 18.0f)

        binding?.tvCommissionDetails?.text = "The commission is $commission%, PGC is $paymentGatewayCharge% and GST is $gst%"
    }

    private fun showDialogBox() {
        val customDialog= Dialog(this)
        val dialogBinding= MainPageDialogLayoutBinding.inflate(layoutInflater)
        customDialog.setContentView(dialogBinding.root)
        customDialog.setCanceledOnTouchOutside(true)
        customDialog.show()
        dialogBinding.btnDismiss.setOnClickListener {
            customDialog.dismiss()
        }
    }
}