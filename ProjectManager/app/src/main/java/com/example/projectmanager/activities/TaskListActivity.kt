package com.example.projectmanager.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectmanager.R
import com.example.projectmanager.adapters.TaskListItemAdapter
import com.example.projectmanager.databinding.ActivityTaskListBinding
import com.example.projectmanager.firebase.FirestoreClass
import com.example.projectmanager.models.Board
import com.example.projectmanager.models.Card
import com.example.projectmanager.models.Task
import com.example.projectmanager.models.User
import com.example.projectmanager.utils.Constants

class TaskListActivity : BaseActivity() {
    private var boardDocumentId:String?=""
    private var binding:ActivityTaskListBinding?=null
    private lateinit var mBoardObject:Board
    lateinit var mAssignedMemberDetailsList:ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if (intent.hasExtra(Constants.DOCUMENT_ID)){
            boardDocumentId  = intent.getStringExtra(Constants.DOCUMENT_ID)
        }

        showProgressDialogBox("Please Wait")
        FirestoreClass().getBoardDetails(this, boardDocumentId!!)

    }

    //for 3dots on status bar action
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_members-> {
                val intent=Intent(this,MemberActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAILS,mBoardObject)
                startActivity(intent)
                return true
            }
            R.id.action_delete_board->{
                alertDialogForDeleteBoard(mBoardObject.name)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupActionBar(title:String){
        setSupportActionBar(binding?.toolbarTaskListActivity)
        val actionBar=supportActionBar
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_color_white)
            actionBar.title=title
        }
        binding?.toolbarTaskListActivity?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            //Refresh the board if update has happened
            showProgressDialogBox("Refreshing..")
            FirestoreClass().getBoardDetails(this, boardDocumentId!!)
        }
    }

    fun cardDetails(taskListPosition:Int,cardPosition:Int){
        val intent=Intent(this,CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAILS,mBoardObject)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION,taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION,cardPosition)
        //passing members list
        intent.putExtra(Constants.BOARD_MEMBERS_LIST,mAssignedMemberDetailsList)
        resultLauncher.launch(intent)
    }

    fun boardDetails(board:Board){
        mBoardObject=board
        hideProgressDialogBox()
        setupActionBar(board.name)



        //get memberlist for carddetailsactivity
        showProgressDialogBox("Fetching Members")
        FirestoreClass().getAssignedMembersListDetails(this,mBoardObject.assignedTo)

    }

    fun addUpdateTaskListSuccess(){
        //Called from Firestore class, this will get the complete board once it has been updated and update to UI as well
        hideProgressDialogBox()
        showProgressDialogBox("Getting Board Details")
        FirestoreClass().getBoardDetails(this,mBoardObject.documentId)
    }

    //This is called from TaskListItemAdapter once tick button is clicked
    fun createTaskList(taskListName:String){
        val task=Task(taskListName,FirestoreClass().getCurrentUserId())
        mBoardObject.taskList.add(0,task)
        mBoardObject.taskList.removeAt(mBoardObject.taskList.size-1) //Remove "Add List" element
        showProgressDialogBox("Please Wait")
        FirestoreClass().addUpdateTaskList(this,mBoardObject)
    }

    fun updateTaskList(position:Int,listName:String,model:Task){
        val task=Task(listName,model.createdBy)

        mBoardObject.taskList[position]=task
        mBoardObject.taskList.removeAt(mBoardObject.taskList.size-1)

        showProgressDialogBox("Updating..")
        FirestoreClass().addUpdateTaskList(this,mBoardObject)
    }

    fun deleteTaskList(position:Int){
        mBoardObject.taskList.removeAt(position)
        mBoardObject.taskList.removeAt(mBoardObject.taskList.size-1)

        showProgressDialogBox("Deleting..")
        FirestoreClass().addUpdateTaskList(this,mBoardObject)
    }

    fun addCardToTaskList(position:Int, cardName:String){
        mBoardObject.taskList.removeAt(mBoardObject.taskList.size-1)
        val cardAssignedUserList:ArrayList<String> = ArrayList()
        val createdBy=FirestoreClass().getCurrentUserId()
        cardAssignedUserList.add(createdBy)

        val card=Card(cardName,createdBy,cardAssignedUserList)
        val cardList= mBoardObject.taskList[position].cards
        cardList.add(card)
        val task=Task(mBoardObject.taskList[position].title,
            mBoardObject.taskList[position].createdBy,
            cardList)
        mBoardObject.taskList[position] = task

        showProgressDialogBox("Adding Card...")
        FirestoreClass().addUpdateTaskList(this,mBoardObject)
    }

    fun getBoardMemberDetailsList(list:ArrayList<User>){
        mAssignedMemberDetailsList=list
        val task=Task("Add List") //this is a UI element
        mBoardObject.taskList.add(task) //We want a dynamic UI, for this, we are adding this as an item to it displayed

        binding?.rvTaskList?.layoutManager=LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL,
            false)
        binding?.rvTaskList?.hasFixedSize()
        val adapter=TaskListItemAdapter(this,mBoardObject.taskList)

        binding?.rvTaskList?.adapter=adapter
        hideProgressDialogBox()
    }

    fun updateCardsInTaskListOnDrag(taskListPosition: Int,cards:ArrayList<Card>){
        mBoardObject.taskList.removeAt(mBoardObject.taskList.size-1) //remove "Add Card" entry

        mBoardObject.taskList[taskListPosition].cards=cards
        showProgressDialogBox("Please Wait")
        FirestoreClass().addUpdateTaskList(this,mBoardObject)
    }

    private fun alertDialogForDeleteBoard(boardName: String) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle(resources.getString(R.string.alert))
        //set message for alert dialog
        builder.setMessage(
            resources.getString(
                R.string.confirmation_message_to_delete_board,
                boardName
            )
        )
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
            // TODO (Step 8: Call the function to delete the card.)
            // START
            FirestoreClass().deleteBoard(this,boardDocumentId!!)
            setResult(Activity.RESULT_OK)
            finish()
            // END
        }
        //performing negative action
        builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }
}