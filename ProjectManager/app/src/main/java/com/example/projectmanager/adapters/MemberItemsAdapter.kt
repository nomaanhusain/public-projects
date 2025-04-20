package com.example.projectmanager.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectmanager.R
import com.example.projectmanager.databinding.ItemMemberBinding
import com.example.projectmanager.models.User
import com.example.projectmanager.utils.Constants

class MemberItemsAdapter(private val context: Context,
                         private val list : ArrayList<User>)
    : RecyclerView.Adapter<MemberItemsAdapter.ViewHolder>(){

    var onMemberClickListener: OnClickListener?=null
    class ViewHolder(binding: ItemMemberBinding):RecyclerView.ViewHolder(binding.root){
        val ivMemberImage=binding.ivMemberImage
        var tvMemberName=binding.tvMemberName
        val tvMemberEmail=binding.tvMemberEmail
        val ivSelectedMember=binding.ivSelectedMember
        val llItemMember=binding.llItemMember
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemMemberBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        Glide.with(context)
            .load(model.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(holder.ivMemberImage)

        holder.tvMemberName.text=model.name
        holder.tvMemberEmail.text=model.email

        if (model.selected){
            holder.ivSelectedMember.visibility=View.VISIBLE
        }else{
            holder.ivSelectedMember.visibility=View.GONE
        }

        holder.llItemMember.setOnClickListener {
            if (onMemberClickListener!=null){
                if (model.selected){
                    onMemberClickListener!!.OnClick(position,model,Constants.UN_SELECT)
                }else{
                    onMemberClickListener!!.OnClick(position,model,Constants.SELECT)
                }
            }
        }
    }

    fun setOnClickListener(onClickListener:OnClickListener){
        this.onMemberClickListener=onClickListener
    }

    //add functionality here to OnClick then override and use in other class
    interface OnClickListener{
        fun OnClick(position:Int, user:User, action:String)
    }
}