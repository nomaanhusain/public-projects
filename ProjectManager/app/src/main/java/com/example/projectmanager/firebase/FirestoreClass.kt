package com.example.projectmanager.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.projectmanager.activities.*
import com.example.projectmanager.models.Board
import com.example.projectmanager.models.User
import com.example.projectmanager.utils.Constants
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        //Create a collection called users
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId()).set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisterSuccess()
            }.addOnFailureListener {
                Log.e("Error in signup:","${it.message}")
            }

    }

    fun getCurrentUserId(): String {
        val currentUser=Firebase.auth.currentUser
        var currentUserId=""
        if (currentUser!=null){
            currentUserId=currentUser.uid
        }
        return currentUserId
    }

    fun updateUserProfileData(activity:Activity,userHashMap:HashMap<String,Any>){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener{document->
                Log.i("Success","Profile updates successfully")
                Toast.makeText(activity,"Profile updates successfully",Toast.LENGTH_SHORT).show()
                when(activity){
                    is MyProfileActivity->{
                        activity.profileUpdateSuccess()
                    }
                    is MainActivity->{
                        activity.tokenUpdateSuccess() //for notification
                    }
                }

            }.addOnFailureListener {
                when(activity) {
                    is MyProfileActivity -> {
                        activity.hideProgressDialogBox()
                        activity.profileUpdateSuccess()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialogBox()
                    }
                }
                Log.e("Error","Error in updating profile")
            }
    }
    fun deleteBoard(activity:TaskListActivity, boardId:String){
        mFireStore.collection(Constants.BOARDS)
            .document(boardId)
            .delete()
            .addOnSuccessListener {
                Log.i("delete","Board deleted successfully")
                Toast.makeText(activity,"Board deleted successfully",Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                activity.hideProgressDialogBox()
                Log.e("Board deletion failed","${it.message}")
            }
    }

    fun createBoard(activity: CreateBoardActivity,board:Board){
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.i("Success","Board created successfully")
                Toast.makeText(activity,"Board created successfully",Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()
            }.addOnFailureListener {
                activity.hideProgressDialogBox()
                Log.e("Board creation failed","${it.message}")
            }
    }

    fun getAllBoardsList(activity:MainActivity){
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO,getCurrentUserId())//this means return document where assignedTo==currentUserID
            .get()
            .addOnSuccessListener {documents->
                Log.i("On Success Listener: ", documents.documents.toString())
                val boardList:ArrayList<Board> = ArrayList()
                for (i in documents.documents){
                    val board = i.toObject(Board::class.java)!!
                    board.documentId=i.id //i.id is a value in document, here we are assigning that
                    println("***---*** board id = ${board.documentId}")
                    boardList.add(board)
                }
                activity.populateBoardsListToUI(boardList)
            }.addOnFailureListener {
                activity.hideProgressDialogBox()
                Log.e("Error getAllBoardsList",it.message!!)
            }
    }

    fun loadUserData(activity: Activity, readBoardsList:Boolean = false) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId()).get()
            .addOnSuccessListener {document->
                val loggedInUser=document.toObject(User::class.java)
                when(activity){
                    is LoginActivity->{
                        if (loggedInUser != null) {
                            activity.signInSuccess(loggedInUser)
                        }
                    }
                    is MainActivity->{
                        if (loggedInUser != null) {
                            activity.updateNavigationUserDetails(loggedInUser, readBoardsList)
                        }
                    }
                    is MyProfileActivity->{
                        if (loggedInUser != null) {
                            activity.updateUI(loggedInUser)
                        }
                    }
                }

            }.addOnFailureListener {
                when(activity){
                    is LoginActivity->{
                        activity.hideProgressDialogBox()
                    }
                    is MainActivity->{
                            activity.hideProgressDialogBox()
                    }
                }
                Log.e("Error in Login:","${it.message}")
            }


    }

    fun addUpdateTaskList(activity: Activity,board: Board){
        //we will do this with hashmap so updating is easy in future
        val taskListHashMap=HashMap<String,Any>()
        taskListHashMap[Constants.TASK_LIST]=board.taskList

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId) //if we pass nothing here, it creates a new collection, else it finds the one passed here
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.i("addUpdateTaskList FS()","Tasklist updated successfully")

                if (activity is TaskListActivity) {
                    activity.addUpdateTaskListSuccess()
                }
                if (activity is CardDetailsActivity){
                    activity.addUpdateTaskListSuccess()
                }
            }.addOnFailureListener {
                Log.e("addUpdateTaskList FS()","Failure: ${it.message!!}")
                if (activity is TaskListActivity) {
                    activity.hideProgressDialogBox()
                }
                if (activity is CardDetailsActivity){
                    activity.hideProgressDialogBox()
                }
            }
    }

    fun getBoardDetails(taskListActivity: TaskListActivity, boardDocumentId: String) {
        mFireStore.collection(Constants.BOARDS)
            .document(boardDocumentId)
            .get()
            .addOnSuccessListener {document->
                Log.i("Get board details: ", document.toString())

                val board = document.toObject(Board::class.java)!!
                board.documentId=document.id //Assign document.id from firebase to board.documentId
                taskListActivity.boardDetails(board)
            }.addOnFailureListener {
                taskListActivity.hideProgressDialogBox()
                Log.e("Error getAllBoardsList",it.message!!)
            }

    }

    fun getAssignedMembersListDetails(activity:Activity, assignedToList:ArrayList<String>){
        mFireStore.collection(Constants.USERS)
            .whereIn(Constants.ID,assignedToList)
            .get()
            .addOnSuccessListener {
                document->
                Log.i("FirestoreClass","assignedMemberListDetails onSuccessListener")

                val usersList:ArrayList<User> = ArrayList()
                for (i in document.documents){
                    usersList.add(i.toObject(User::class.java)!!)
                }
                if (activity is MemberActivity){
                    activity.setupMembersList(usersList)
                }
                if (activity is TaskListActivity){
                    activity.getBoardMemberDetailsList(usersList)
                }
            }.addOnFailureListener {
                if (activity is MemberActivity){
                    activity.hideProgressDialogBox()
                }
                if (activity is TaskListActivity){
                    activity.hideProgressDialogBox()
                }
                Log.e("FirestoreClass","assignedMemberListDetails ${it.message}")
            }
    }

    fun getMemberDetails(activity: MemberActivity,email:String){
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL,email)
            .get()
            .addOnSuccessListener {document->
                Log.i("FirestoreClass","getMemberDetails onSuccessListener")
                if (document.documents.size>0){
                    val user=document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                }else{
                    activity.hideProgressDialogBox()
                    activity.displayErrorSnackbar("No such user found")
                }
            }.addOnFailureListener {
                activity.hideProgressDialogBox()
                Log.i("FirestoreClass","getMemberDetails ${it.message}")
            }
    }

    fun assignMemberToBoard(activity: MemberActivity,board:Board,user: User){
        val assignedToHashMap=HashMap<String,Any>()

        assignedToHashMap[Constants.ASSIGNED_TO]=board.assignedTo

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignedSuccess(user)
            }.addOnFailureListener {
                activity.hideProgressDialogBox()
                Log.e("FirestoreClass","assignMemberToBoard ${it.message}")
            }
    }
}