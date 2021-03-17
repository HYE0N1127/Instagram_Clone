package com.example.instagram_clone.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram_clone.R
import com.example.instagram_clone.navigation.model.contentDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class DetailViewFragment : Fragment() {
    var firestore: FirebaseFirestore? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail, container, false)

        firestore = FirebaseFirestore.getInstance()
        return view
    }

    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var contentDTOs: ArrayList<contentDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()


        init {
            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()

                for(snapshot in querySnapshot!!.documents) {
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

        private fun CustomViewHolder(view: View): RecyclerView.ViewHolder {

        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var viewholder = (p0 as CustomViewHolder).itemView
        }

    }

}