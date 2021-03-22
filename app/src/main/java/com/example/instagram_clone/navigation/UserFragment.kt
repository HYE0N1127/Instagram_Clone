package com.example.instagram_clone.navigation

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.instagram_clone.LoginActivity
import com.example.instagram_clone.MainActivity
import com.example.instagram_clone.R
import com.example.instagram_clone.navigation.model.AlarmDTO
import com.example.instagram_clone.navigation.model.FollowDTO
import com.example.instagram_clone.navigation.model.contentDTO
import com.example.instagram_clone.navigation.util.FcmPush
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_user.view.*

class UserFragment : Fragment() {
    var fragmentView: View? = null
    var firestore: FirebaseFirestore? = null
    var uid: String? = null
    var auth: FirebaseAuth? = null
    var currentUserUid: String? = null  // 나의 계정인지 다른 사람의 계정인지 분류하기 위한 변수

    companion object {
        var PICK_PROFILE_FROM_ALBUM = 10
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView =
            LayoutInflater.from(activity).inflate(R.layout.fragment_user, container, false)
        uid = arguments?.getString("destinationUid")
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserUid = auth?.currentUser?.uid

        if (uid == currentUserUid) {
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
            mainactivity?.toolbar_btn_back.visibility = View.VISIBLE        //툴바의 뒤로가기 버튼을 보이게 해줌
            fragmentView?.account_btn_follow_signout?.setOnClickListener {
                requestFollow()
            }
        }

        fragmentView?.account_recyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.account_recyclerview?.layoutManager = GridLayoutManager(activity!!, 3)

        fragmentView?.account_iv_profile?.setOnClickListener {      //UserFragment에 프로필 사진 올리기
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)
        }
        getProfileImage()
        getFollowerAndFollowing()
        return fragmentView
    }

    fun getFollowerAndFollowing() {     //Follower와 Following을 텍스트로 표시해주는 함수
        //내 페이지를 클릭했을 때는 내 uid, 상대방 페이지는 상대방 uid
        firestore?.collection("users")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot == null) return@addSnapshotListener

            var followDTO = documentSnapshot.toObject(FollowDTO::class.java)

            if(followDTO?.followingCount != null) {
                fragmentView?.account_tv_following_count?.text = followDTO?.followingCount?.toString()
            }

            if(followDTO?.followerCount != null) {
                fragmentView?.account_tv_follower_count?.text = followDTO?.followerCount?.toString()

                if(followDTO?.followers?.containsKey(currentUserUid!!)) {       //팔로워를 하고있다면 버튼이 변환
                    fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow_cancel)
                    fragmentView?.account_btn_follow_signout?.background?.setColorFilter(ContextCompat.getColor(activity!!, R.color.colorLightGray), PorterDuff.Mode.MULTIPLY)

                } else {
                    if(uid != currentUserUid) {     //코드 안정성을 위하여 uid와 currentUserId가 같지 않다면을 넣어줌
                        fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow)
                        fragmentView?.account_btn_follow_signout?.background?.colorFilter = null
                    }
                }
            }
        }
    }

    fun requestFollow() {
        //상대 누구를 팔로우하는지 저장
        var tsDocFollowing = firestore?.collection("users")?.document(currentUserUid!!)

        firestore?.runTransaction { transaction ->
            var followDTO =
                transaction.get(tsDocFollowing!!).toObject(FollowDTO::class.java) //누구를 팔로우하는지 받아옴

            if (followDTO == null) {
                followDTO == FollowDTO()

                followDTO!!.followingCount = 1  //팔로잉 카운트를 1로 만듬
                followDTO!!.followers[uid!!] = true     //중복 팔로잉 방지를 위해 UID를 넣어줌

                transaction.set(tsDocFollowing, followDTO)  //DB에 데이터가 담김

                return@runTransaction

            }

            if (followDTO.followings.containsKey(uid)) {                //내가 팔로우한 상태라면?
                //팔로잉 취소
                followDTO?.followingCount = followDTO?.followingCount - 1
                followDTO?.followers.remove(uid)        //UID를 삭제함으로써 팔로잉 목록에서 삭제

            } else {
                //팔로잉 추가
                followDTO?.followingCount = followDTO?.followingCount + 1
                followDTO?.followers[uid!!] = true
            }
            transaction.set(tsDocFollowing, followDTO)      //DB 저장
            return@runTransaction
        }

        //타인이 누구를 팔로우 하는지 저장
        var tsDocFollower = firestore?.collection("users")?.document(uid!!)

        firestore?.runTransaction { transaction ->
            var followDTO =
                transaction.get(tsDocFollower!!).toObject(FollowDTO::class.java)        //값을 읽어옴

            if (followDTO == null) {
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUserUid!!] = true
                followerAlarm(uid!!)        //최초로 누군가가 팔로우를 할 때 알람을 보내줌

                transaction.set(tsDocFollower, followDTO!!)
                return@runTransaction
            }

            if (followDTO!!.followers.containsKey(currentUserUid)) {
                //팔로우를 했을 경우
                followDTO!!.followerCount = followDTO!!.followerCount - 1
                followDTO!!.followers.remove(currentUserUid)

            } else {
                //팔로우를 안했을 경우
                followDTO!!.followerCount = followDTO!!.followerCount + 1
                followDTO!!.followers[currentUserUid!!] = true
                followerAlarm(uid!!)        //팔로우를 하였다는 알람을 보내줌

            }
            transaction.set(tsDocFollower, followDTO!!)     //DB에 값 저장
            return@runTransaction
        }
    }

    fun followerAlarm(destinationUid: String) {
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = auth?.currentUser?.email
        alarmDTO.uid = auth?.currentUser?.uid
        alarmDTO.kind = 2
        alarmDTO.timestamp = System.currentTimeMillis()
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

        var message = auth?.currentUser?.email + getString(R.string.alarm_follow)
        FcmPush.instance.sendMessage(destinationUid, "Howlstagram", message)
    }

    fun getProfileImage() {     //올린 이미지를 다운받는 함수
        firestore?.collection("profileImages")?.document(uid!!)
            ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (documentSnapshot == null) return@addSnapshotListener

                if (documentSnapshot.data != null) {
                    var url = documentSnapshot?.data!!["image"]     //Image주소가 넘어옴
                    Glide.with(activity!!).load(url).apply(RequestOptions().circleCrop())
                        .into(fragmentView?.account_iv_profile!!)
                }
            }
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