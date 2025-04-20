package com.example.projectmanager.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.projectmanager.R
import com.example.projectmanager.firebase.FirestoreClass
import com.example.projectmanager.models.User
import com.example.projectmanager.utils.Constants
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

class MyProfileActivity : BaseActivity() {
    private var toolbar:Toolbar?=null
    private var civProfileImage:CircleImageView?=null
    private var btnUpdateMyProfile:AppCompatButton?=null

    private lateinit var galleryImageResultLauncher: ActivityResultLauncher<Intent>
    private var mImageUri:Uri?=null
    private var mImageFirebaseURL:String?=null
    private lateinit var mUserDetails:User
    private var etName:EditText?=null
    private var etEmail:EditText?=null
    private var etMobile:EditText?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        toolbar=findViewById(R.id.toolbarMyProfileActivity)
        civProfileImage=findViewById(R.id.civUserImageMyProfile)
        btnUpdateMyProfile=findViewById(R.id.btnUpdateMyProfile)

        setupActionBar()

        //This updates UI with correct image and other data
        FirestoreClass().loadUserData(this)

        registerOnActivityForGalleryResult()
        civProfileImage?.setOnClickListener {
            Constants.selectImageFromGallery(this,galleryImageResultLauncher)
        }

        btnUpdateMyProfile?.setOnClickListener {
            if (mImageUri!=null){
                uploadUserImage()
            }else{
                updateUserProfileDataInFirebase()
            }
        }
        etName=findViewById(R.id.etNameMyProfile)
        etEmail=findViewById(R.id.etEmailMyProfile)
        etMobile=findViewById(R.id.etMobileMyProfile)

    }

    //exported to constants
    /*private fun selectImageFromGallery() {
        Dexter.withContext(this).withPermission(
            Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(object :PermissionListener{
            override fun onPermissionGranted(report: PermissionGrantedResponse?) {
                println("Storage Permission is granted - MyProfileActivity.kt")
                val galleryIntent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galleryImageResultLauncher.launch(galleryIntent)

            }

            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                println("Storage Permission is denied - MyProfileActivity.kt")
                showRationalDialogForPermission()
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: PermissionRequest?,
                p1: PermissionToken?
            ) {
                println("Storage Permission RationalShouldBeShown - MyProfileActivity.kt")
                showRationalDialogForPermission()
            }

        }).onSameThread().check()
    }*/

    private fun registerOnActivityForGalleryResult(){
        galleryImageResultLauncher=registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){ result ->
            if (result.resultCode== Activity.RESULT_OK){
                val data:Intent?=result.data
                if(data!=null){
                    val contentUri = data.data
                    try {
                        mImageUri=contentUri
                        var bitmap:Bitmap?=null
                        if(Build.VERSION.SDK_INT < 28) {
                            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentUri)
                            println("Got Image Bitmap[DEPRECATED] - MyProfileActivity.kt")
                        }else {
                            val source = ImageDecoder.createSource(this.contentResolver, contentUri!!)
                            bitmap = ImageDecoder.decodeBitmap(source)

                            //val bitmap : Bitmap =MediaStore.Images.Media.getBitmap(this.contentResolver,contentUri)
                            println("Got Image Bitmap - MyProfileActivity.kt")
                        }
                        Glide
                            .with(this)
                            .load(mImageUri)
                            .centerCrop()
                            .placeholder(R.drawable.ic_user_place_holder)
                            .into(civProfileImage!!)
                    }catch (e: IOException){
                        e.printStackTrace()
                        Toast.makeText(this,
                            "Failed to load image from gallery",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    //exported to constants
    /*private fun showRationalDialogForPermission() {
        AlertDialog.Builder(this)
            .setMessage("It looks like you have denied the permission"+
                    ", this permission is required for this feature and can be enabled in Setting -> Apps")
            .setPositiveButton("GO TO SETTINGS"){
                    _,_ ->
                //Launch into app settings
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri= Uri.fromParts("package",packageName,null)
                    intent.data=uri
                    startActivity(intent)
                }catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel"){
                    dialog,which ->
                dialog.dismiss()
            }.show()
    }*/

    private fun setupActionBar(){
        setSupportActionBar(toolbar)
        val actionBar=supportActionBar
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_color_white)
            actionBar.title="My Profile"
        }
        toolbar?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun updateUserProfileDataInFirebase(){
        showProgressDialogBox("Updating backend..")
        val userHashMap=HashMap<String,Any>()
        var updateRequired=false
        if (!mImageFirebaseURL.isNullOrEmpty()  && mImageFirebaseURL != mUserDetails.image){
            userHashMap[Constants.IMAGE]= mImageFirebaseURL!!
            updateRequired=true
        }
        if (etName?.text.toString() != mUserDetails.name){
            userHashMap[Constants.NAME]=etName?.text.toString()
            updateRequired=true
        }
        if (etMobile?.text.toString().toLong() != mUserDetails.mobileNumber){
            userHashMap[Constants.MOBILENUMBER]=etMobile?.text.toString().toLong()
            updateRequired=true
        }
        if (updateRequired) {
            FirestoreClass().updateUserProfileData(this, userHashMap)
        }
    }

    //Called from FirestoreClass, this updates ui with image from url
    fun updateUI(user:User){

        mUserDetails=user



        //Glide can easily load image from url
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(civProfileImage!!)

        etName?.setText(user.name)
        etEmail?.setText(user.email)
        if (user.mobileNumber!=0L){
            etMobile?.setText(user.mobileNumber.toString())
        }
    }

    private fun uploadUserImage(){
        showProgressDialogBox("Uploading Image..")
        if (mImageUri!=null){
            Log.i("mImageUri = ",mImageUri.toString())
            val sRef:StorageReference=FirebaseStorage.getInstance().reference.child(
                "USER-IMAGE"+System.currentTimeMillis()+"."+Constants.getFileExtension(this,mImageUri!!)
            )

            sRef.putFile(mImageUri!!).addOnSuccessListener {
                taskSnapshot->
                Log.i("Firebase Image URL",taskSnapshot.metadata?.reference?.downloadUrl.toString())

                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener {
                    uri->
                    Log.i("Downloadable image url",uri.toString())
                    mImageFirebaseURL=uri.toString()

                    hideProgressDialogBox()
                    updateUserProfileDataInFirebase()
                }
            }.addOnFailureListener{
                Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
                hideProgressDialogBox()
            }
        }
    }

    //Get extention of the file eg. png or jpg
//    private fun getFileExtension(uri: Uri):String?{
//        var extn= MimeTypeMap.getSingleton().getMimeTypeFromExtension(
//            contentResolver.getType(uri))
//        println("THE EXTENTION IS = $extn")
//        return extn
//    }

    fun profileUpdateSuccess(){
        hideProgressDialogBox()
        //This is for when we click update button, this will set the result to RESULT_OK which
        //will signal the main activity "registerForActivityResult" thing that its now time to call the
        //FirestoreClass function to update the side nav menu bar
        setResult(Activity.RESULT_OK)
        finish()
    }
}