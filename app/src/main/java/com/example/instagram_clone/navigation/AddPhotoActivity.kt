package com.example.instagram_clone.navigation

import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.instagram_clone.R
import com.example.instagram_clone.navigation.model.contentDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0
    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null
    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        //initate storage

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        //open the album
        var photoPickerIntent = Intent(Intent.ACTION_PICK)

        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        //add image upload event

        addphoto_btn_upload.setOnClickListener {
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            if (resultCode == Activity.RESULT_OK) {
                // This is path to the selected Image
                photoUri = data?.data

                addphoto_image.setImageURI(photoUri)

            } else {
                // Exit the addPhotoActivity if you leave the album
                finish()
            }
        }
    }

    fun contentUpload() {
        //make filename

        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"

        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

        //Promise method
//        storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
//            return@continueWithTask storageRef.downloadUrl
//        }?.addOnSuccessListener { uri ->
//            var contentDTO = contentDTO()
//
//            // Insert downloaderUrl of image
//            contentDTO.imageUrl = uri.toString()
//
//            //Insert UID of user
//            contentDTO.uid = auth?.currentUser?.uid
//
//            //Insert UserId
//            contentDTO.uid = auth?.currentUser?.email
//
//            //Insert ezplain of content
//            contentDTO.explain = addphoto_edit_explain.text.toString()
//
//            //Insert Timestamp
//            contentDTO.timestamp = System.currentTimeMillis()
//
//            firestore?.collection("images")?.document()?.set(contentDTO)
//
//            setResult(Activity.RESULT_OK)
//
//            finish()
//        }

        //CallBack Method
        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                var contentDTO = contentDTO()

                // Insert downloaderUrl of image
                contentDTO.imageUrl = uri.toString()

                //Insert UID of user
                contentDTO.uid = auth?.currentUser?.uid

                //Insert UserId
                contentDTO.uid = auth?.currentUser?.email

                //Insert ezplain of content
                contentDTO.explain = addphoto_edit_explain.text.toString()

                //Insert Timestamp
                contentDTO.timestamp = System.currentTimeMillis()

                firestore?.collection("images")?.document()?.set(contentDTO)

                setResult(Activity.RESULT_OK)

                finish()
            }
        }
    }
}