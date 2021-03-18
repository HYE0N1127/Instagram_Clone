package com.example.instagram_clone.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagram_clone.R
import com.example.instagram_clone.navigation.model.contentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

class DetailViewFragment : Fragment() {
    var firestore: FirebaseFirestore? = null
    var uid = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail, container, false)

        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.detailviewfragment_recyclerview.adapter = DetailViewRecyclerViewAdapter()
        view.detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity)

        return view
    }

    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var contentDTOs: ArrayList<contentDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()


        init {
            firestore?.collection("images")?.orderBy("timestamp")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    contentDTOs.clear()
                    contentUidList.clear()

                    for (snapshot in querySnapshot!!.documents) {
                        var item = snapshot.toObject(contentDTO::class.java)
                        contentDTOs.add(item!!)
                        contentUidList.add(snapshot.id)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(p0: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(p0.context).inflate(R.layout.item_detail, p0, false)

            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var viewholder = (p0 as CustomViewHolder).itemView

            //userID
            viewholder.detailViewItem_Profile_TextView.text = contentDTOs!![p1].userId

            //Image
            Glide.with(p0.itemView.context).load(contentDTOs!![p1].imageUrl)
                .into(viewholder.detailViewItem_ImageView_Content)

            //Explain of Content
            viewholder.detailViewItem_explain_Textview.text = contentDTOs!![p1].explain

            //likes
            viewholder.detailViewItem_FavoriteCounter_Textview.text =
                "Likes " + contentDTOs!![p1].favoriteCount

            //profile Image
            Glide.with(p0.itemView.context).load(contentDTOs!![p1].imageUrl)
                .into(viewholder.detailViewItem_Profile_image)

            //This code is when the button clicked
            viewholder.detailViewItem_Favorite_Imageview.setOnClickListener {
                favoriteEvent(p1)
            }

            //This code is when the page is loaded
            if (contentDTOs!![p1].favorites.containsKey(uid)) {
                //This is like status
                viewholder.detailViewItem_Favorite_Imageview.setImageResource(R.drawable.ic_favorite)
            } else {
                //This is unlike status
                viewholder.detailViewItem_Favorite_Imageview.setImageResource(R.drawable.ic_favorite_border)
            }
        }

        fun favoriteEvent(position: Int) {
            var tsdoc = firestore?.collection("images")?.document(contentUidList[position])

            firestore?.runTransaction { transaction ->


                var contentDTO = transaction.get(tsdoc!!).toObject(contentDTO::class.java)

                if (contentDTO!!.favorites.containsKey(uid)) {
                    //When the button is clicked
                    contentDTO?.favoriteCount = contentDTO.favoriteCount!! - 1
                    contentDTO?.favorites.remove(uid)
                } else {
                    //when the button is not clicked
                    contentDTO?.favoriteCount = contentDTO.favoriteCount!! + 1
                    contentDTO?.favorites[uid!!] = true
                }

                transaction.set(tsdoc, contentDTO)
            }
        }
    }

}