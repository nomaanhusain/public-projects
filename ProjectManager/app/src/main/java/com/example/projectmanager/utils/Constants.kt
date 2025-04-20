package com.example.projectmanager.utils

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.widget.Toolbar
import com.example.projectmanager.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

object Constants {
    const val FCM_TOKEN_UPDATED: String="fcm_token_updated"
    const val FCM_TOKEN="fcmToken"
    const val EMAIL: String="email"
    const val USERS: String = "users"

    const val NAME: String = "name"
    const val IMAGE = "image"
    const val MOBILENUMBER = "mobileNumber"

    const val BOARDS:String="boards"
    const val DOCUMENT_ID="documentId"
    const val ASSIGNED_TO="assignedTo"
    const val TASK_LIST="taskList"
    const val BOARD_DETAILS="boardDetails"
    const val ID="id"
    const val BOARD_MEMBERS_LIST:String="board_members_list"
    const val SELECT:String="Select"
    const val UN_SELECT:String="un_select"
    const val PROJMANAGER_PREFERENCES:String="Projectmanager_preferences"

    const val TASK_LIST_ITEM_POSITION="task_list_item_position"
    const val CARD_LIST_ITEM_POSITION="card_list_item_position"


    const val FCM_BASE_URL:String = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION:String = "Authorization"
    const val FCM_KEY:String = "key"
    const val FCM_SERVER_KEY:String = "RETRACTED"
    const val FCM_KEY_TITLE:String = "title"
    const val FCM_KEY_MESSAGE:String = "message"
    const val FCM_KEY_DATA:String = "data"
    const val FCM_KEY_TO:String = "to"

    fun selectImageFromGallery(activity:Activity,galleryImageResultLauncher:ActivityResultLauncher<Intent>) {
        Dexter.withContext(activity).withPermission(
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
                showRationalDialogForPermission(activity)
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: PermissionRequest?,
                p1: PermissionToken?
            ) {
                println("Storage Permission RationalShouldBeShown - MyProfileActivity.kt")
                showRationalDialogForPermission(activity)
            }

        }).onSameThread().check()
    }


    fun showRationalDialogForPermission(activity: Activity) {
        AlertDialog.Builder(activity)
            .setMessage("It looks like you have denied the permission"+
                    ", this permission is required for this feature and can be enabled in Setting -> Apps")
            .setPositiveButton("GO TO SETTINGS"){
                    _,_ ->
                //Launch into app settings
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri= Uri.fromParts("package",activity.packageName,null)
                    intent.data=uri
                    activity.startActivity(intent)
                }catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel"){
                    dialog,which ->
                dialog.dismiss()
            }.show()
    }

    fun getFileExtension(activity: Activity,uri: Uri):String?{
        var extn= MimeTypeMap.getSingleton().getMimeTypeFromExtension(
            activity.contentResolver.getType(uri))
        println("THE EXTENTION IS = $extn")
        return extn
    }

}
