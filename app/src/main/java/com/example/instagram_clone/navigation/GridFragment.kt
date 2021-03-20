package com.example.instagram_clone.navigation

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.instagram_clone.R
import com.example.instagram_clone.navigation.model.contentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_grid.view.*

class GridFragment : Fragment() {
    var firestore: FirebaseFirestore? = null
    var fragmentView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView =
            LayoutInflater.from(activity).inflate(R.layout.fragment_grid, container, false)
        firestore = FirebaseFirestore.getInstance()

        fragmentView?.gridfragment_recyclerview?.adapter =
            UserFragmentRecyclerViewAdapter()        //Grid Fragment 안에 있는 UserFragment를 넣어줘야함

        fragmentView?.gridfragment_recyclerview?.layoutManager = GridLayoutManager(activity, 3)

        return fragmentView
    }

    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs: ArrayList<contentDTO> = ArrayList()

        init {
            firestore?.collection("images")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    //Somtimes, This code return null of querySnapshot when it signout
                    if (querySnapshot == null) return@addSnapshotListener

                    //Get Data
                    for (snapshot in querySnapshot.documents) {
                        contentDTOs.add(snapshot.toObject(contentDTO::class.java)!!)
                    }
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