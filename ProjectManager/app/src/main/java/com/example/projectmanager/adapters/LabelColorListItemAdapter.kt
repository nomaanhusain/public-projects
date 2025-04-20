package com.example.projectmanager.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projectmanager.databinding.ItemLabelColorBinding

class LabelColorListItemAdapter(
    private val context: Context,
    private val list : ArrayList<String>,
    private val mSelectedColor:String) : RecyclerView.Adapter<LabelColorListItemAdapter.ViewHolder>() {

    lateinit var onItemClickListener: OnItemClickListener

    class ViewHolder(binding: ItemLabelColorBinding):RecyclerView.ViewHolder(binding.root) {
        val flMain= binding.flMain
        val viewMain = binding.viewMain
        val ivSelectedColor=binding.ivSelectedColor
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemLabelColorBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val colorItem=list[position] //list will define the colors and their sequence on the screen
        holder.viewMain.setBackgroundColor(Color.parseColor(colorItem))//set color of each element acc to list
        if (colorItem == mSelectedColor){
            holder.ivSelectedColor.visibility=View.VISIBLE
        }else{
            holder.ivSelectedColor.visibility=View.GONE
        }

        holder.flMain.setOnClickListener {
            if (onItemClickListener!=null){
                onItemClickListener!!.OnClick(position,colorItem)
            }
        }
    }

    fun setOnClickListener(onItemClickListener:OnItemClickListener){
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener{
        fun OnClick(position:Int, color:String)
    }

}