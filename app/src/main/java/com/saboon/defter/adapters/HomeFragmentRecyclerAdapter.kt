package com.saboon.defter.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.saboon.defter.R
import com.saboon.defter.models.ModelHomeSection

class HomeFragmentRecyclerAdapter(private val sectionList: List<ModelHomeSection>): RecyclerView.Adapter<HomeFragmentRecyclerAdapter.HomeFragmentViewHolder>() {

    class HomeFragmentViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.textView_homeRecyclerView_date)
        val senderFirst: ImageView = view.findViewById(R.id.imageView_homeRecyclerView_senderFirst)
        val senderSecond: ImageView = view.findViewById(R.id.imageView_homeRecyclerView_senderSecond)
        val childRecyclerView: RecyclerView = view.findViewById(R.id.recyclerView_homeRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeFragmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_row_home_fragment,parent,false)
        return HomeFragmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeFragmentViewHolder, position: Int) {
        holder.date.text = sectionList[position].date
        val sendersPp = sectionList[position].sendersPp
        when(sendersPp.size){
            1->{
                holder.senderFirst.visibility = View.VISIBLE
                Glide.with(holder.itemView.context)
                    .load(sendersPp[0])
                    .into(holder.senderFirst)
            }
            2->{
                holder.senderFirst.visibility = View.VISIBLE
                holder.senderSecond.visibility = View.VISIBLE
                Glide.with(holder.itemView.context)
                    .load(sendersPp[0])
                    .into(holder.senderFirst)
                Glide.with(holder.itemView.context)
                    .load(sendersPp[1])
                    .into(holder.senderSecond)
            }
        }

        holder.childRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.childRecyclerView.adapter = HomeFragmentRecyclerAdapterChild(sectionList[position].moments)
        holder.childRecyclerView.addItemDecoration(
            DividerItemDecoration(holder.itemView.context, DividerItemDecoration.VERTICAL)
        )
    }

    override fun getItemCount(): Int {
        return sectionList.size
    }
}