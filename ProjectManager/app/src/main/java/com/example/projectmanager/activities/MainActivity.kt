package com.example.projectmanager.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectmanager.R
import com.example.projectmanager.adapters.BoardItemsAdapter
import com.example.projectmanager.databinding.ActivityMainBinding
import com.example.projectmanager.firebase.FirestoreClass
import com.example.projectmanager.models.Board
import com.example.projectmanager.models.User
import com.example.projectmanager.utils.Constants
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var binding:ActivityMainBinding?=null
    //this is for when we update any info, it should change in the nav bar as well
    private val startUpdateActivityAndGetResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                //This will update the data in the side nav bar
                FirestoreClass().loadUserData(this)
            } else {
                Log.e("onActivityResult()", "Profile update cancelled by user")
            }
        }

    var fabCreateBoard:FloatingActionButton?=null
    private lateinit var mUserName:String
    private var rvBoards:RecyclerView?=null
    private var tvNoBoardsAvailable:TextView?=null
    private lateinit var mSharedPreferences:SharedPreferences




    //To update mainactivity once a new board is created and we return to mainactivity
    private val boardLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if(result.resultCode == Activity.RESULT_OK){
            FirestoreClass().getAllBoardsList(this)
        }
    }

    //To update mainactivity once a board is deleted and we return to mainactivity
    private val boardOnClickLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if(result.resultCode == Activity.RESULT_OK){
            FirestoreClass().getAllBoardsList(this)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        rvBoards=findViewById(R.id.rvBoardsList)
        tvNoBoardsAvailable=findViewById(R.id.tvNoBoardsAvailable)

        mSharedPreferences=this.getSharedPreferences(Constants.PROJMANAGER_PREFERENCES,Context.MODE_PRIVATE)

        val isTokenUpdated=mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED,false)

        if (isTokenUpdated){
            showProgressDialogBox("isTokenUpdated")
            FirestoreClass().loadUserData(this,true)
            Log.e("FCM token","already exist From main activity")
            println("****** FCM token already exist From main activity")
        }else {
            /**
             * Check this if InvalidRegistration http failure from doInBackground
             */
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                val msg = getString(R.string.msg_token_fmt, token)
                updateFCMToken(token)
                Log.e("FCM token", "From main activity new created $token")
                println("******** FCM token From main activity new created $token")
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            }
        }
        setupActionBar()
        //this will add functionality from our  overriden function to nav view
        binding?.navView?.setNavigationItemSelectedListener(this)

        FirestoreClass().loadUserData(this,true)

        fabCreateBoard=findViewById(R.id.fabCreateBoard)
        fabCreateBoard?.setOnClickListener {
            val intent=Intent(this,CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME,mUserName)
            boardLauncher.launch(intent)
        }
    }


    private fun setupActionBar(){
        //accessing different layouts through binding MainActivity->app_bar_main->Toolbar
        setSupportActionBar(binding?.appBarMainLayout?.toolbarMainActivity)
        binding?.appBarMainLayout?.toolbarMainActivity?.setNavigationIcon(R.drawable.ic_navigation_menu)
        binding?.appBarMainLayout?.toolbarMainActivity?.setNavigationOnClickListener {
            toggleSideDrawer()
        }
    }

    private fun toggleSideDrawer() {
        if (binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true){
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        }else{
            binding?.drawerLayout?.openDrawer(GravityCompat.START)
        }
    }

     fun populateBoardsListToUI(boardsList:ArrayList<Board>){
        hideProgressDialogBox()

        if (boardsList.size > 0){
            rvBoards?.visibility=View.VISIBLE
            tvNoBoardsAvailable?.visibility=View.INVISIBLE

            rvBoards?.layoutManager=LinearLayoutManager(this)
            rvBoards?.setHasFixedSize(true)
            val adapter=BoardItemsAdapter(this,boardsList)
            rvBoards?.adapter=adapter


            //Click listener for individual board element
            adapter.setOnClickListener(object :BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity,TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID,model.documentId)
                    boardOnClickLauncher.launch(intent)
                }

            })

        }else{
            rvBoards?.visibility=View.GONE
            tvNoBoardsAvailable?.visibility=View.VISIBLE
        }
    }

    override fun onBackPressed() {
        //If drawer is open, close it otherwise normal functionality of double back to exit
        //if you create seperate binding then it will not be available in this view
        if (binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true){
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        }else{
            doubleBackToExit()
        }
    }

    //when we click on items in side drawer
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.navMyProfile->{
                Toast.makeText(this,"My profile",Toast.LENGTH_SHORT).show()
                startUpdateActivityAndGetResult.launch(Intent(this,MyProfileActivity::class.java))
            }
            R.id.navSignOut->{
                Firebase.auth.signOut()
                val intent =Intent(this,IntroActivity::class.java)
                //These flags are for avaoiding memory overhead, please ctrl+click on these and read about it
                //These are very important
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                mSharedPreferences.edit().clear().apply()
                startActivity(intent)
                finish()
            }
        }
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        return true
    }


    //This is called from FirestoreClass load user data
    fun updateNavigationUserDetails(loggedInUser: User, readBoardsList:Boolean) {
        hideProgressDialogBox()
        mUserName=loggedInUser.name

        val civImage:CircleImageView= findViewById(R.id.civUserImage)
        val tvNavHeaderUsername:TextView=findViewById(R.id.tvNavHeaderUsername)
        Glide
            .with(this)
            .load(loggedInUser.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(civImage)

        tvNavHeaderUsername.text=loggedInUser.name

        if (readBoardsList){
            showProgressDialogBox("Reading Board List")
            FirestoreClass().getAllBoardsList(this)
        }
    }

    fun tokenUpdateSuccess() {
        hideProgressDialogBox()
        val editor:SharedPreferences.Editor=mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED,true)
        editor.apply()

        showProgressDialogBox("token update")
        FirestoreClass().loadUserData(this,true)
    }

    /**
     * To update FCM Token in DB
     */
    private fun updateFCMToken(token:String){
        val userHashMap=HashMap<String,Any>()
        userHashMap[Constants.FCM_TOKEN]=token
        showProgressDialogBox("Updating FCM Token")
        FirestoreClass().updateUserProfileData(this,userHashMap)
    }
}