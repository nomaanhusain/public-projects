package com.example.projectmanager.activities

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.example.projectmanager.R
import com.example.projectmanager.databinding.ActivitySignUpBinding
import com.example.projectmanager.firebase.FirestoreClass
import com.example.projectmanager.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : BaseActivity() {
    private var binding:ActivitySignUpBinding?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySignUpBinding.inflate(layoutInflater)
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

        binding?.toolbarSignUpActivity?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding?.btnSignUp?.setOnClickListener {
            registerUser()
        }

    }

    fun userRegisterSuccess(){
        hideProgressDialogBox()
        Toast.makeText(
            this,
            "Registed email from firebase",
            Toast.LENGTH_SHORT
        ).show()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = Firebase.auth.currentUser
        if(currentUser != null){
            Toast.makeText(this,"User already signed in",Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(binding?.toolbarSignUpActivity)

        val actionBar=supportActionBar
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_color_back)
        }
    }
    private fun registerUser(){
        val name= binding?.etName?.text?.toString()?.trim(){it <= ' '}
        val email= binding?.etEmail?.text?.toString()?.trim(){it <= ' '}
        val password= binding?.etPassword?.text?.toString()
        if (validateForm(name,email,password)){
            showProgressDialogBox("Please Wait")
            Firebase.auth
                .createUserWithEmailAndPassword(email!!,password!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user=User(firebaseUser.uid,name!!,registeredEmail)
                        FirestoreClass().registerUser(this,user)
                    } else {
                        hideProgressDialogBox()
                        Toast.makeText(
                            this,
                            "Error = ${task.exception!!.message}", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun validateForm(name:String?,email:String?,password:String?):Boolean{
        return when{
            TextUtils.isEmpty(name)->{
                displayErrorSnackbar("Enter name")
                false
            }
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
}