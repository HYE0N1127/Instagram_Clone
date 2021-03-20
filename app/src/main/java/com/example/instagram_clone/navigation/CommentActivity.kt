package com.example.instagram_clone.navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.instagram_clone.R
import com.example.instagram_clone.navigation.model.contentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_comment.*

class CommentActivity : AppCompatActivity() {
    var contentUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        comment_btn_send?.setOnClickListener {
            var comment = contentDTO.comment()

            comment.userId = FirebaseAuth.getInstance().currentUser?.email      //이메일값 받아오기
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid   //UID 받아오기
            comment.comments = comment_edit_message.text.toString()
            comment.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("images").document(contentUid!!)
                .collection("comments").document().set(comment)

            comment_edit_message.setText("")        //문자 초기화
        }
    }
}