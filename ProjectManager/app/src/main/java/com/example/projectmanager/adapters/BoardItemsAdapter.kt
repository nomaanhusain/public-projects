package com.example.projectmanager.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectmanager.R
import com.example.projectmanager.databinding.BoardItemBinding
import com.example.projectmanager.models.Board

open class BoardItemsAdapter(private val context:Context,
                             private val list : ArrayList<Board>)
    :RecyclerView.Adapter<BoardItemsAdapter.ViewHolder>(){

    private var onClickListener:OnClickListener?=null


    class ViewHolder(binding: BoardItemBinding):RecyclerView.ViewHolder(binding.root){
        val llBoardItem=binding.llBoardItem
        val civBoardImage=binding.civBoardItemBoardImage
        val tvBoardName=binding.tvBoardItemBoardName
        val tvBoardCreatedBy=binding.tvBoardItemCreatedBy
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(BoardItemBinding
            .inflate(LayoutInflater.from(parent.context),
                parent,
                false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model=list[position]

            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(holder.civBoardImage)

        holder.tvBoardName.text=model.name
        holder.tvBoardCreatedBy.text="Created by: "+model.createdBy

        holder.llBoardItem.setOnClickListener {
            if(onClickListener!=null){
                onClickListener!!.onClick(position,model)
            }
        }
    }

    //will implement in mainactivity
    interface OnClickListener{
        fun onClick(position: Int,model: Board)
    }

    //we will override this in mainactivity
    fun setOnClickListener(onc:OnClickListener){
        this.onClickListener=onc
    }
}