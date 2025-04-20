package com.example.projectmanager.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.projectmanager.R
import com.example.projectmanager.databinding.ActivityCreateBoardBinding
import com.example.projectmanager.firebase.FirestoreClass
import com.example.projectmanager.models.Board
import com.example.projectmanager.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class CreateBoardActivity : BaseActivity() {
    private var binding:ActivityCreateBoardBinding?=null

    private lateinit var galleryImageResultLauncher: ActivityResultLauncher<Intent>
    private var mImageUri: Uri?=null
    private var mBoardImageFirestoreURL:String=""

    private lateinit var mUserName:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()
        registerOnActivityForGalleryResult()
        if (intent.hasExtra(Constants.NAME)){
            mUserName=intent.getStringExtra(Constants.NAME)!!
        }
        binding?.civCreateBoard?.setOnClickListener {
            Constants.selectImageFromGallery(this,galleryImageResultLauncher)
        }


        binding?.btnCreateCreateBoard?.setOnClickListener {
            if (mImageUri!=null){
                uploadBoardImage()
            }else{
                createBoard()
            }
        }

    }
    private fun setupActionBar(){
        //accessing different layouts through binding MainActivity->app_bar_main->Toolbar
        setSupportActionBar(binding?.toolbarCreateBoardActivity)
        val actionBar=supportActionBar
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_color_white)
            actionBar.title="Create Board"
        }
        binding?.toolbarCreateBoardActivity?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun createBoard(){
        showProgressDialogBox("Creating Board")
        val assignedUserArrayList:ArrayList<String> = ArrayList()
        assignedUserArrayList.add(getCurrentUserId())

        val board=Board(
            binding?.etBoardNameCreateBoard?.text.toString(),
            mBoardImageFirestoreURL,
            mUserName,
            assignedUserArrayList
        )

        FirestoreClass().createBoard(this,board)
    }

    fun boardCreatedSuccessfully(){
        hideProgressDialogBox()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun uploadBoardImage(){
        showProgressDialogBox("Uploading Image..")
        if (mImageUri!=null){
            Log.i("mImageUri Board= ",mImageUri.toString())
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "BOARD-IMAGE"+System.currentTimeMillis()+"."+Constants.getFileExtension(this,mImageUri!!)
            )

            sRef.putFile(mImageUri!!).addOnSuccessListener {
                    taskSnapshot->
                Log.i("Firebase BoardImage URL",taskSnapshot.metadata?.reference?.downloadUrl.toString())

                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener {
                        uri->
                    Log.i("Downloadable image url",uri.toString())
                    mBoardImageFirestoreURL=uri.toString()

                    hideProgressDialogBox()
                    createBoard()
                    //TODO(Update UI with board)
                }
            }.addOnFailureListener{
                Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
                hideProgressDialogBox()
            }
        }
    }

    private fun registerOnActivityForGalleryResult(){
        galleryImageResultLauncher=registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){ result ->
            if (result.resultCode== Activity.RESULT_OK){
                val data:Intent?=result.data
                if(data!=null){
                    val contentUri = data.data
                    try {
                        mImageUri=contentUri
                        var bitmap: Bitmap?=null
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
                            .into(binding?.civCreateBoard!!)
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
}