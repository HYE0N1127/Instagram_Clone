package com.example.instagram_clone.navigation

import android.content.Intent
import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.instagram_clone.LoginActivity
import com.example.instagram_clone.MainActivity
import com.example.instagram_clone.R
import com.example.instagram_clone.navigation.model.contentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_user.view.*

class UserFragment : Fragment() {
    var fragmentView: View? = null
    var firestore: FirebaseFirestore? = null
    var uid: String? = null
    var auth: FirebaseAuth? = null
    var currentUserUid: String? = null  // 나의 계정인지 다른 사람의 계정인지 분류하기 위한 변수


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user, container, false)
        uid = arguments?.getString("destinationUid")
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserUid = auth?.currentUser?.uid

        if(uid == currentUserUid) {
            //my Page
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.signout)
            fragmentView?.account_btn_follow_signout?.setOnClickListener {
                activity?.finish()

                startActivity(Intent(activity, LoginActivity::class.java))
                auth?.signOut()
            }
        } else {
            //Other User Page
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow)

            var mainactivity = (activity as MainActivity) // 누구의 유저페이지인지 보여주는 Back 버튼과 TextView 활성화

            mainactivity?.toolbar_username?.text = arguments?.getString("userId")
            mainactivity?.toolbar_btn_back?.setOnClickListener {
                mainactivity.bottom_navigation.selectedItemId = R.id.action_home
            }
            mainactivity?.toolbar_title_image?.visibility = View.GONE       //타이틀 이미지를 안보이게 해줌
            mainactivity?.toolbar_username?.visibility = View.VISIBLE       //유저네임을 보이게 해줌
        }

        fragmentView?.account_recyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.account_recyclerview?.layoutManager = GridLayoutManager(activity!!, 3)

        return fragmentView
    }






    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs: ArrayList<contentDTO> = ArrayList()

        init {
            firestore?.collection("images")?.whereEqualTo("uid", uid)
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    //Somtimes, This code return null of querySnapshot when it signout
                    if (querySnapshot == null) return@addSnapshotListener

                    //Get Data
                    for (snapshot in querySnapshot.documents) {
                        contentDTOs.add(snapshot.toObject(contentDTO::class.java)!!)
                    }
                    fragmentView?.account_tv_post_count?.text = contentDTOs.size.toString()
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3
            var imageView = ImageView(p0.context)

            imageView.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            return CustomViewHolder(imageView)
        }

        inner class CustomViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var imageView = (p0 as CustomViewHolder).imageView
            Glide.with(p0.itemView.context).load(contentDTOs[p1].imageUrl)
                .apply(RequestOptions().centerCrop()).into(imageView)
        }
    }
}