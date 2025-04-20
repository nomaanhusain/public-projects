package com.example.projectmanager.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectmanager.R
import com.example.projectmanager.databinding.ItemCardSelectedMemberBinding
import com.example.projectmanager.models.SelectedMembers
import com.example.projectmanager.models.User

open class CardMemberListItemAdapter(private val context: Context, private val list:ArrayList<SelectedMembers>,
                                     private val membersAssignable:Boolean):RecyclerView.Adapter<CardMemberListItemAdapter.ViewHolder>() {


    var onCardMemberClickListener:OnClickListener?=null
    class ViewHolder(binding: ItemCardSelectedMemberBinding): RecyclerView.ViewHolder(binding.root){
        val ivSelectedMemberImage=binding.ivSelectedMemberImage
        val ivAddMember=binding.ivAddMember
        val llCardSelectedMember=binding.llCardSelectedMember

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemCardSelectedMemberBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        if (position==list.size-1 && membersAssignable){ //last position
            holder.ivAddMember.visibility=View.VISIBLE
            holder.ivSelectedMemberImage.visibility=View.GONE
        }else{
            holder.ivAddMember.visibility=View.GONE
            holder.ivSelectedMemberImage.visibility=View.VISIBLE

            Glide.with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(holder.ivSelectedMemberImage)
        }

        holder.llCardSelectedMember.setOnClickListener {
            if (onCardMemberClickListener!=null){
                onCardMemberClickListener!!.OnClick()
            }
        }
    }

    fun setOnClickListener(onClickListener:OnClickListener){
        this.onCardMemberClickListener=onClickListener
    }

    //add functionality here to OnClick then override and use in other class
    interface OnClickListener{
        fun OnClick()
    }

}