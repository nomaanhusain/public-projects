package com.nomaanh.deliveryappcommissioncalculator

import android.app.Dialog
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.nomaanh.deliveryappcommissioncalculator.databinding.ActivityGetMoneyBinding
import com.nomaanh.deliveryappcommissioncalculator.databinding.GetMoneyDialogLayoutBinding

class GetMoneyActivity : AppCompatActivity() {
    private var binding: ActivityGetMoneyBinding?=null
    private var commission:Float?=null
    private var pgc:Float?=null
    private var gst:Float?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityGetMoneyBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.ivInfoBtn?.setOnClickListener{
            customDialogForInfo()
        }


        //For commission
        val commsn: SharedPreferences = getSharedPreferences("commission", MODE_PRIVATE)
        commission = commsn.getFloat("valueComm", 22.0f)

        //For PGC
        val pgcPref: SharedPreferences = getSharedPreferences("paymentGatewayCharge", MODE_PRIVATE)
        pgc = pgcPref.getFloat("valuePgc", 1.84f)

        //For GST
        val gstPref: SharedPreferences = getSharedPreferences("gst", MODE_PRIVATE)
        gst = gstPref.getFloat("valueGst", 18.0f)

        binding?.btnCalculate?.setOnClickListener {
            calculateGetMoney()
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        binding=null
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this@GetMoneyActivity.finish()
    }

    private fun getPGCAmt(price:Float):Double{
        return (price*1.05)*(pgc!!/100)
    }

    private fun calculateGetMoney() {
        if (binding?.etPriceOnApp?.text.toString().isEmpty()){
            Toast.makeText(this,
                "Please enter a price", Toast.LENGTH_LONG).show()
        }else{
            val inputPrice=(binding?.etPriceOnApp?.text.toString()).toFloat()
            val commAmt=inputPrice*(commission!!/100)
            val commGst=commAmt*(gst!!/100)
            val pgcAmt=getPGCAmt(inputPrice).toFloat()
            val pgcTax=pgcAmt*(gst!!/100)
            val totalDeductions=commAmt+commGst+pgcAmt+pgcTax
            val totalDeductStr="Total deductions are = $totalDeductions"
            val amtInAcc=inputPrice-(commAmt+commGst+pgcAmt+pgcTax)
            val detailsStr="Price on App = $inputPrice\nCommission ($commission%) = $commAmt\nGST on Commission ($gst%) = $commGst\n" +
                    "Payment gateway charge ($pgc%) = $pgcAmt\nGST on PGC ($gst%) = $pgcTax"+
                    "\n$totalDeductStr"
            val finalStr="Amount you will receive in bank = $amtInAcc"

            binding?.tvDetailsGetMoney?.text=detailsStr
            binding?.tvFinalPriceGetMoney?.text=finalStr
        }
    }

    private fun customDialogForInfo(){
        val customDialog= Dialog(this)
        val dialogBinding= GetMoneyDialogLayoutBinding.inflate(layoutInflater)
        customDialog.setContentView(dialogBinding.root)
        customDialog.setCanceledOnTouchOutside(true)
        customDialog.show()
        dialogBinding.btnDismiss.setOnClickListener {
            customDialog.dismiss()
        }
    }
}