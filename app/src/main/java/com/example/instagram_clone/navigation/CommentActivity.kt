package com.example.instagram_clone.navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.instagram_clone.R
import com.example.instagram_clone.navigation.model.contentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.item_comment.view.*

class CommentActivity : AppCompatActivity() {
    var contentUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        contentUid = intent.getStringExtra("contentUid")

        comment_recyclerview.adapter = commentRecyclerViewAdapter()
        comment_recyclerview.layoutManager = LinearLayoutManager(this)

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

    inner class commentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var comments: ArrayList<contentDTO.comment> = arrayListOf()

        init {
            FirebaseFirestore.getInstance().collection("images").document(contentUid!!)
                .collection("comments")     //Comment들을
                .orderBy("timestamp")       //시간순으로 정렬
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (querySnapshot == null) return@addSnapshotListener       //프로그램의 안정성

                    for (snapshot in querySnapshot.documents!!) {
                        comments.add(snapshot.toObject(contentDTO.comment::class.java)!!)
                    }
                    notifyDataSetChanged()      //새로고침
                }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(p0.context).inflate(R.layout.item_comment, p0, false)

            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return comments.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var view = p0.itemView
            view.commentviewitem_textview_comment.text = comments[p1].comments      //커멘트 매핑
            view.commentviewitem_textview_profile.text = comments[p1].userId        //유저 아이디 매핑

            FirebaseFirestore.getInstance().collection("profileImages")     //커멘트를 단 프로필의 주소가 넘어옴
                .document(comments[p1].uid!!).get().addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        var url = task.result!!["images"]

                        Glide.with(p0.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(view.commentviewtiem_imageview_profile)
                    }
                }
        }


    }
}