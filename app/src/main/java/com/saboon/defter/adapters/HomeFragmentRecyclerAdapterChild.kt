package com.saboon.defter.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.saboon.defter.R
import com.saboon.defter.models.ModelMoments

class HomeFragmentRecyclerAdapterChild(private val moments:List<ModelMoments>):RecyclerView.Adapter<HomeFragmentRecyclerAdapterChild.HomeFragmentChildViewHolder>() {

    class HomeFragmentChildViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val userName: TextView = view.findViewById(R.id.textView_homeRecyclerViewChild_userName)
        val userComment: TextView = view.findViewById(R.id.textView_homeRecyclerViewChild_userComment)
        val momentImage: ImageView = view.findViewById(R.id.imageView_homeRecyclerViewChild_post)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeFragmentChildViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_row_home_fragment_child,parent,false)
        return HomeFragmentChildViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeFragmentChildViewHolder, position: Int) {
        Glide.with(holder.itemView.context)
            .load(moments[position].resizedPhotoURL)
            .into(holder.momentImage)
        holder.userName.text = moments[position].senderUserName
        holder.userComment.text = moments[position].text
    }

    override fun getItemCount(): Int {
        return moments.size
    }
}