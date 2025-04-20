package com.example.projectmanager.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.example.projectmanager.R
import com.example.projectmanager.databinding.ActivityLoginBinding
import com.example.projectmanager.firebase.FirestoreClass
import com.example.projectmanager.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : BaseActivity() {
    private var binding:ActivityLoginBinding?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityLoginBinding.inflate(layoutInflater)
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

        setupActionBar()

        binding?.toolbarSignInActivity?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding?.btnSignIn?.setOnClickListener {
            signInUser()
        }

    }
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = Firebase.auth.currentUser
        if(currentUser != null){
            Toast.makeText(this,"User already signed in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInUser(){
        val email= binding?.etEmail?.text?.toString()?.trim(){it <= ' '}
        val password= binding?.etPassword?.text?.toString()
        if (validateForm(email,password)){
            showProgressDialogBox("Signing In")
            Firebase.auth.signInWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        // Sign in success, update UI with the signed-in user's information
                        println("Main Activity Sign In request")
                        FirestoreClass().loadUserData(this@LoginActivity)
                    } else {
                        hideProgressDialogBox()
                        // If sign in fails, display a message to the user.
                        Toast.makeText(baseContext, "Error ${task.exception!!.message}",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }

    }

    private fun validateForm(email:String?,password:String?):Boolean{
        return when{
            TextUtils.isEmpty(email)->{
                displayErrorSnackbar("Enter email")
                false
            }
            TextUtils.isEmpty(password)->{
                displayErrorSnackbar("Enter password")
                false
            }
            else -> true
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarSignInActivity)

        val actionBar=supportActionBar
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_color_back)
        }
    }

    //this is called from FirestoreClass
    fun signInSuccess(loggedInUser: User) {
        println("Login activity hide progress bar code below")
        hideProgressDialogBox()
        startActivity(Intent(this,MainActivity::class.java))
        finish()

    }
}