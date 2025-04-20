package com.example.projectmanager.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectmanager.activities.TaskListActivity
import com.example.projectmanager.databinding.ItemCardBinding
import com.example.projectmanager.models.Card
import com.example.projectmanager.models.SelectedMembers

class CardListItemAdapter(private val context: Context,
                          private val list : ArrayList<Card>)
    :RecyclerView.Adapter<CardListItemAdapter.ViewHolder>(){

    private lateinit var onClickListener:OnClickListener

    class ViewHolder(binding: ItemCardBinding):RecyclerView.ViewHolder(binding.root) {
        val tvCardName=binding.tvCardName
        val tvMembersName=binding.tvMembersName
        val cvItemCard=binding.cvItemCard
        val viewLabelColor=binding.viewLabelColor
        val rvCardSelectedMembersList=binding.rvCardSelectedMembersList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewBinding = ItemCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)

        return ViewHolder(viewBinding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        holder.tvCardName.text=model.name

        if ((context as TaskListActivity).mAssignedMemberDetailsList.size>0){
            val selectedMembersList:ArrayList<SelectedMembers> = ArrayList()

            for (i in context.mAssignedMemberDetailsList.indices){
                for (j in model.assignedTo){
                    if (context.mAssignedMemberDetailsList[i].id==j){
                        val selectedMembers=SelectedMembers(context.mAssignedMemberDetailsList[i].id,
                        context.mAssignedMemberDetailsList[i].image)
                        selectedMembersList.add(selectedMembers)
                    }
                }
            }
            if (selectedMembersList.size>0){
                //if you are the only person assigned, then dont display the image here
                if (selectedMembersList.size == 1 && selectedMembersList[0].id==model.createdBy){
                    holder.rvCardSelectedMembersList.visibility=View.GONE
                }else{
                    holder.rvCardSelectedMembersList.visibility=View.VISIBLE

                    holder.rvCardSelectedMembersList.layoutManager=GridLayoutManager(context,4)
                    val adapter=CardMemberListItemAdapter(context,selectedMembersList,false)
                    holder.rvCardSelectedMembersList.adapter=adapter
                    adapter.setOnClickListener(object :CardMemberListItemAdapter.OnClickListener{
                        override fun OnClick() {
                            if (onClickListener!=null){
                                onClickListener!!.OnClick(position)
                            }
                        }
                    })
                }
            }else{
                holder.rvCardSelectedMembersList.visibility=View.GONE
            }
        }

        if (model.labelColor.isNotEmpty()){
            holder.viewLabelColor.visibility=View.VISIBLE
            holder.viewLabelColor.setBackgroundColor(Color.parseColor(model.labelColor))
        }else{
            holder.viewLabelColor.visibility=View.GONE
        }
        holder.cvItemCard.setOnClickListener{
            if (onClickListener!=null){
                onClickListener.OnClick(position) //pass position of card to TaskListItemAdapter
            }
        }
    }

    fun setOnClickListener(onClickListener:OnClickListener){
        this.onClickListener=onClickListener
    }

    interface OnClickListener{
        fun OnClick(cardPosition:Int)
    }
}