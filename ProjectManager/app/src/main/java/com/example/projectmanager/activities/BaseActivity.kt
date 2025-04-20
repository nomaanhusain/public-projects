package com.example.projectmanager.activities

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.projectmanager.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

open class BaseActivity : AppCompatActivity() {

    private var doubleBackToExitPressedOnce=false
    private var mProgressDialog:Dialog?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }

    fun showProgressDialogBox(text:String){
        mProgressDialog= Dialog(this)

        //this is how you inflate a loyout of a different binding eg. of a a dialog
        mProgressDialog?.setContentView(R.layout.dialog_custom_progress)
        mProgressDialog?.findViewById<TextView>(R.id.tvProgressDialog)?.text=text
        mProgressDialog?.show()
    }
    fun hideProgressDialogBox(){
        if(mProgressDialog!=null){
            mProgressDialog?.dismiss()
            mProgressDialog= null
        }
    }
    fun getCurrentUserId():String{
        return FirebaseAuth.getInstance().currentUser!!.uid
    }
    fun doubleBackToExit(){
        if(doubleBackToExitPressedOnce){
            onBackPressedDispatcher.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce=true
        Toast.makeText(this,"Press back once more to exit application",Toast.LENGTH_SHORT).show()

        //we want to reset this functionallity after 3 sec as imagine if user pressed back after a few minutes it would exit
        Handler(Looper.getMainLooper()).postDelayed({
            this.doubleBackToExitPressedOnce=false
        },3000)
    }

    fun displayErrorSnackbar(message:String){
        val snackbar=Snackbar.make(findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG)
        val snackbarView=snackbar.view
        snackbarView.setBackgroundColor(ContextCompat.getColor(this,R.color.snackbar_error_color))
        snackbar.show()
    }
}