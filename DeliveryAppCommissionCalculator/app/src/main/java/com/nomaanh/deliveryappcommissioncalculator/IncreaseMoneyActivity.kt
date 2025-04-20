package com.nomaanh.deliveryappcommissioncalculator

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.nomaanh.deliveryappcommissioncalculator.databinding.ActivityIncreaseMoneyBinding
import com.nomaanh.deliveryappcommissioncalculator.databinding.IncreaseMoneyDialogLayoutBinding

class IncreaseMoneyActivity : AppCompatActivity() {
    private var binding: ActivityIncreaseMoneyBinding?=null
    private var commission:Float?=null
    private var pgc:Float?=null
    private var gst:Float?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityIncreaseMoneyBinding.inflate(layoutInflater)
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
            calculateIncrease()
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        binding=null
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this@IncreaseMoneyActivity.finish()
    }

    private fun getPGCAmt(price:Float):Double{
        return (price*1.05)*(pgc!!/100)
    }
    private fun calculateIncrease(){
        if(binding?.etPriceOnMenu?.text.toString().isEmpty()){
            Toast.makeText(this,
                "Please enter a price",
                Toast.LENGTH_LONG).show()
        }else{
            val inputPrice:Float = (binding?.etPriceOnMenu?.text.toString()).toFloat()

            val priceOnApp:Float=inputPrice/(1-((commission!!/100)+(commission!!/100)*(gst!!/100)+
                    (pgc!!/100)+(pgc!!/100)*(gst!!/100)))
            val commissionAmt=priceOnApp*(commission!!/100)
            val gstOnCom=commissionAmt*(gst!!/100)
            val pgcAmt=getPGCAmt(priceOnApp).toFloat()
            val pgcGst=pgcAmt*(gst!!/100)
            val totalDeductions=commissionAmt+gstOnCom+pgcAmt+pgcGst
            val totalDeductStr="Total deductions are = $totalDeductions"
            val inputStr="Price on menu = $inputPrice"
            val comStr="Commission ($commission%) for App = $commissionAmt"
            val gstStr="GST ($gst%) on commission = $gstOnCom"
            val pgcStr="Payment gateway charge ($pgc%) = $pgcAmt"
            val pgcGstStr="GST on PGC ($gst%) = $pgcGst"
            val finalPriceStr="The price on the app should be = $priceOnApp"
            binding?.tvDetails?.text="$inputStr\n$comStr\n$gstStr\n$pgcStr\n$pgcGstStr\n$totalDeductStr"
            binding?.tvFinalPrice?.text="$finalPriceStr"
        }
    }

    private fun customDialogForInfo(){
        val customDialog= Dialog(this)
        val dialogBinding= IncreaseMoneyDialogLayoutBinding.inflate(layoutInflater)
        customDialog.setContentView(dialogBinding.root)
        customDialog.setCanceledOnTouchOutside(true)
        customDialog.show()
        dialogBinding.btnDismiss.setOnClickListener {
            customDialog.dismiss()
        }
    }
}