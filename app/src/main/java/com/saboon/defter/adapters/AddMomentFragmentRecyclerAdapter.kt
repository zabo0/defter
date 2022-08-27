package com.saboon.defter.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.saboon.defter.R
import com.saboon.defter.fragments.AddNewMomentFragmentDirections
import de.hdodenhof.circleimageview.CircleImageView

class AddMomentFragmentRecyclerAdapter(private val dailyMomentPhotos_ID_URL_list: ArrayList<String>):RecyclerView.Adapter<AddMomentFragmentRecyclerAdapter.ViewHolder>() {



    class ViewHolder(view: View):RecyclerView.ViewHolder(view) {
        val imgView : CircleImageView = view.findViewById(R.id.dailyMomentsIMG)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_row_add_moment_daily_moments,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.itemView.context)
            .load(getURL(dailyMomentPhotos_ID_URL_list[position]))
            .into(holder.imgView)

        holder.itemView.setOnClickListener {
            val action = AddNewMomentFragmentDirections.actionAddNewMomentFragmentToMomentPreviewerFragment(getID(dailyMomentPhotos_ID_URL_list[position]))
            it.findNavController().navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return dailyMomentPhotos_ID_URL_list.size
    }

    fun updateAllList(newList: ArrayList<String>){
        dailyMomentPhotos_ID_URL_list.clear()
        dailyMomentPhotos_ID_URL_list.addAll(newList)
        notifyDataSetChanged()
    }

    fun insertNewItem(newItem: String){
        dailyMomentPhotos_ID_URL_list.add(0,newItem)
        notifyItemInserted(0)
        notifyItemChanged(0)
    }

    private fun getURL(value:String):String{
        val stringArray = value.split("+")
        return stringArray[1]
    }

    private fun getID(value:String):String{
        val stringArray = value.split("+")
        return stringArray[0]
    }
}