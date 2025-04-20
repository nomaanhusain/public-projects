package com.example.projectmanager.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.projectmanager.R
import com.example.projectmanager.adapters.CardMemberListItemAdapter
import com.example.projectmanager.databinding.ActivityCardDetailsBinding
import com.example.projectmanager.dialogs.LabelColorListDialog
import com.example.projectmanager.dialogs.SelectMemberListDialog
import com.example.projectmanager.firebase.FirestoreClass
import com.example.projectmanager.models.*
import com.example.projectmanager.utils.Constants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity : BaseActivity() {
    private var binding:ActivityCardDetailsBinding?=null
    private lateinit var mBoardObject:Board
    private var taskListPosition:Int=-1
    private var cardPosition:Int=-1
    private var mSelectedColor:String=""
    private lateinit var mMemberDetailList:ArrayList<User>
    private var mSelectedDateMilis:Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityCardDetailsBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        getIntentData()
        setupActionBar(
            mBoardObject.taskList[taskListPosition].cards[cardPosition].name
        )

        binding?.etNameCardDetails?.setText(mBoardObject.taskList[taskListPosition].cards[cardPosition].name)

        //when we click on et text, we want our cursor to go at end, for that below line
        binding?.etNameCardDetails?.setSelection(binding?.etNameCardDetails?.text.toString().length)

        binding?.btnUpdateCardDetails?.setOnClickListener {
            if (binding?.etNameCardDetails?.text.toString().isNotEmpty()){
                updateCardDetails()
            }else{
                Toast.makeText(
                    this,
                    "Please enter a card name",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding?.tvSelectLabelColor?.setOnClickListener {
            showLabelColorListDialog()
        }

        mSelectedColor=mBoardObject.taskList[taskListPosition].cards[cardPosition].labelColor

        if (mSelectedColor.isNotEmpty()){
            setColor()
        }

        binding?.tvSelectMembers?.setOnClickListener {
            showSelectMemberListDialog()
        }

        setupSelectedMembersList()

        mSelectedDateMilis=mBoardObject.taskList[taskListPosition].cards[cardPosition].dueDate
        if (mSelectedDateMilis>0){
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val formattedDate=sdf.format(mSelectedDateMilis)
            binding?.tvSelectDueDate?.text=formattedDate
        }
        binding?.tvSelectDueDate?.setOnClickListener {
            showDataPicker()
        }

    }

    private fun setupActionBar(title:String){
        setSupportActionBar(binding?.toolbarCardDetailsActivity)
        val actionBar=supportActionBar
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_color_white)
            actionBar.title=title
        }
        binding?.toolbarCardDetailsActivity?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    fun addUpdateTaskListSuccess(){
        //Called from Firestore class, this will get the complete board once it has been updated and update to UI as well
        hideProgressDialogBox()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun deleteCard(){
        var cardList:ArrayList<Card> = mBoardObject.taskList[taskListPosition].cards
        cardList.removeAt(cardPosition)

        var taskList:ArrayList<Task> = mBoardObject.taskList
        taskList.removeAt(taskList.size - 1) //To remove "Add Card" button in the end

        taskList[taskListPosition].cards=cardList
        mBoardObject.taskList=taskList

        setResult(Activity.RESULT_OK)
        finish()

        FirestoreClass().addUpdateTaskList(this,mBoardObject)
    }

    private fun alertDialogForDeleteCard(cardName: String) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle(resources.getString(R.string.alert))
        //set message for alert dialog
        builder.setMessage(
            resources.getString(
                R.string.confirmation_message_to_delete_card,
                cardName
            )
        )
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
            // START
            deleteCard()
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

    //Update board with updated card
    private fun updateCardDetails(){
        val card = Card(
            binding?.etNameCardDetails?.text.toString(),
            mBoardObject.taskList[taskListPosition].cards[cardPosition].createdBy,
            mBoardObject.taskList[taskListPosition].cards[cardPosition].assignedTo,
            mSelectedColor,
            mSelectedDateMilis
        )
        mBoardObject.taskList[taskListPosition].cards[cardPosition] = card

        val taskList:ArrayList<Task> = mBoardObject.taskList
        taskList.removeAt(taskList.size-1)//we need to remove last element that is the add icon in firebase

        showProgressDialogBox("Updating..")
        FirestoreClass().addUpdateTaskList(this@CardDetailsActivity,mBoardObject)
    }

    //Inflate the menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_delete_card->{
                alertDialogForDeleteCard(mBoardObject.taskList[taskListPosition]
                    .cards[cardPosition].name)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun prepColorsList():ArrayList<String>{
        val colorsList: ArrayList<String> = ArrayList()
        colorsList.add("#43C86F")
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#770000")
        colorsList.add("#0022F8")

        return colorsList
    }
    private fun setColor(){
        binding?.tvSelectLabelColor?.text=""
        binding?.tvSelectLabelColor?.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    private fun showLabelColorListDialog(){
        val colorList:ArrayList<String> = prepColorsList()

        val listDialog = object : LabelColorListDialog(
            this,
            colorList,
            resources.getString(R.string.str_select_label_color),
            mSelectedColor
        ){
            override fun onItemSelected(color: String) {
                mSelectedColor=color
                setColor()
            }
        }
        listDialog.show()

    }

    private fun showSelectMemberListDialog(){
        var cardAssignedMembersList=mBoardObject.taskList[taskListPosition].cards[cardPosition].assignedTo

        if (cardAssignedMembersList.size>0){
            for (i in mMemberDetailList.indices){ //member of board
                for (j in cardAssignedMembersList){ //members of card assignedTo arraylist
                    if (mMemberDetailList[i].id == j){
                        mMemberDetailList[i].selected=true
                    }
                }
            }
        }else{
            for (i in mMemberDetailList.indices){
                mMemberDetailList[i].selected=false
            }
        }

        val memberDialogList=object :SelectMemberListDialog(
            this,mMemberDetailList,"Select Member"){
            override fun onMemberSelected(user: User,action: String) {
                if (action== Constants.SELECT){
                    if (!mBoardObject.taskList[taskListPosition].cards[cardPosition].assignedTo.contains(user.id)){
                        mBoardObject.taskList[taskListPosition].cards[cardPosition].assignedTo.add(user.id)
                    }
                    }
                    if (action==Constants.UN_SELECT){
                        if (mBoardObject.taskList[taskListPosition].cards[cardPosition].assignedTo.contains(user.id)){
                            mBoardObject.taskList[taskListPosition].cards[cardPosition].assignedTo.remove(user.id)

                            for (i in mMemberDetailList.indices){ //we need to unselect for the UI as well
                                if (mMemberDetailList[i].id==user.id){
                                    mMemberDetailList[i].selected=false
                                }
                            }
                        }
                    }
                setupSelectedMembersList()
                }
            }
        memberDialogList.show()
    }

    private fun getIntentData(){
        if (intent.hasExtra(Constants.BOARD_DETAILS)){
            if (Build.VERSION.SDK_INT<33){
                mBoardObject= intent.getParcelableExtra(Constants.BOARD_DETAILS)!!
            }else{
                mBoardObject= intent.getParcelableExtra(Constants.BOARD_DETAILS, Board::class.java)!!
            }
        }

        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)){
            taskListPosition=intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION,-1)
        }
        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            cardPosition=intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION,-1)
        }
        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)){
            if (Build.VERSION.SDK_INT<33){
                mMemberDetailList=intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
            }else{
                mMemberDetailList=intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST,User::class.java)!!
            }
        }
    }

    private fun setupSelectedMembersList(){
        val cardAssignedMembersList=mBoardObject.taskList[taskListPosition].cards[cardPosition].assignedTo

        val selectedMembers:ArrayList<SelectedMembers> = ArrayList()

        for (i in mMemberDetailList.indices){ //member of board
            for (j in cardAssignedMembersList){ //members of card assignedTo arraylist
                if (mMemberDetailList[i].id == j){
                    val sMember=SelectedMembers(
                        mMemberDetailList[i].id,
                        mMemberDetailList[i].image
                    )
                    selectedMembers.add(sMember)
                }
            }
        }

        if (selectedMembers.size>0){
            selectedMembers.add(SelectedMembers("",""))//for the add symbol at the end
            binding?.tvSelectMembers?.visibility=View.GONE
            binding?.rvSelectedMembersList?.visibility=View.VISIBLE

            binding?.rvSelectedMembersList?.layoutManager=GridLayoutManager(this,6)
            val adapter=CardMemberListItemAdapter(this,selectedMembers,true)
            binding?.rvSelectedMembersList?.adapter=adapter

            adapter.setOnClickListener(object :CardMemberListItemAdapter.OnClickListener{
                override fun OnClick() {
                    showSelectMemberListDialog()
                }
            })
        }else{
            binding?.tvSelectMembers?.visibility=View.VISIBLE
            binding?.rvSelectedMembersList?.visibility=View.GONE
        }
    }


    /**
     * The function to show the DatePicker Dialog and select the due date.
     */
    private fun showDataPicker() {
        /**
         * This Gets a calendar using the default time zone and locale.
         * The calender returned is based on the current time
         * in the default time zone with the default.
         */
        val c = Calendar.getInstance()
        val year =
            c.get(Calendar.YEAR) // Returns the value of the given calendar field. This indicates YEAR
        val month = c.get(Calendar.MONTH) // This indicates the Month
        val day = c.get(Calendar.DAY_OF_MONTH) // This indicates the Day

        /**
         * Creates a new date picker dialog for the specified date using the parent
         * context's default date picker dialog theme.
         */
        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                /*
                  The listener used to indicate the user has finished selecting a date.
                 Here the selected date is set into format i.e : day/Month/Year
                  And the month is counted in java is 0 to 11 so we need to add +1 so it can be as selected.

                 Here the selected date is set into format i.e : day/Month/Year
                  And the month is counted in java is 0 to 11 so we need to add +1 so it can be as selected.*/

                // Here we have appended 0 if the selected day is smaller than 10 to make it double digit value.
                val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                // Here we have appended 0 if the selected month is smaller than 10 to make it double digit value.
                val sMonthOfYear =
                    if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"

                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
                // Selected date it set to the TextView to make it visible to user.
                binding?.tvSelectDueDate?.text = selectedDate

                /**
                 * Here we have taken an instance of Date Formatter as it will format our
                 * selected date in the format which we pass it as an parameter and Locale.
                 * Here I have passed the format as dd/MM/yyyy.
                 */
                /**
                 * Here we have taken an instance of Date Formatter as it will format our
                 * selected date in the format which we pass it as an parameter and Locale.
                 * Here I have passed the format as dd/MM/yyyy.
                 */
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

                // The formatter will parse the selected date in to Date object
                // so we can simply get date in to milliseconds.
                val theDate = sdf.parse(selectedDate)

                /** Here we have get the time in milliSeconds from Date object
                 */

                /** Here we have get the time in milliSeconds from Date object
                 */
                mSelectedDateMilis = theDate!!.time
            },
            year,
            month,
            day
        )
        dpd.show() // It is used to show the datePicker Dialog.
    }
}