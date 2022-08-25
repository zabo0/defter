package com.saboon.defter.adapters

import android.app.Application
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.saboon.defter.R
import com.saboon.defter.viewmodels.AddNewMomentFragmentViewModel
import de.hdodenhof.circleimageview.CircleImageView

class AddMomentFragmentRecyclerAdapter(private val dailyMomentPhotoURLsList: ArrayList<String>):RecyclerView.Adapter<AddMomentFragmentRecyclerAdapter.ViewHolder>() {



    class ViewHolder(view: View):RecyclerView.ViewHolder(view) {
        val imgView : CircleImageView = view.findViewById(R.id.dailyMomentsIMG)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_row_add_moment_daily_moments,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.itemView.context)
            .load(dailyMomentPhotoURLsList[position])
            .into(holder.imgView)
    }

    override fun getItemCount(): Int {
        return dailyMomentPhotoURLsList.size
    }

    fun updateAllList(newList: ArrayList<String>){
        dailyMomentPhotoURLsList.clear()
        dailyMomentPhotoURLsList.addAll(newList)
        notifyDataSetChanged()
    }

    fun insertNewItem(newItem: String){
        dailyMomentPhotoURLsList.add(0,newItem)
        notifyItemInserted(0)
        notifyItemChanged(0)
    }
}