package com.example.projectmanager.adapters

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectmanager.R
import com.example.projectmanager.activities.TaskListActivity
import com.example.projectmanager.databinding.ItemTaskBinding
import com.example.projectmanager.models.Task
import java.util.Collections

open class TaskListItemAdapter(private val context: Context,
                               private val list : ArrayList<Task>)
    : RecyclerView.Adapter<TaskListItemAdapter.ViewHolder>(){


    private var mPositionDraggedFrom:Int=-1
    private var mPositionDraggedTo:Int=-1

    class ViewHolder(binding: ItemTaskBinding):RecyclerView.ViewHolder(binding.root){
        val tvAddTaskList= binding.tvAddTaskList
        val cvAddTaskListName=binding.cvAddTaskListName
        val llTaskItem=binding.llTaskItem
        val llTitleView=binding.llTitleView
        val cvEditTaskListName=binding.cvEditTaskListName
        val cvAddCard=binding.cvAddCard
        val tvAddCard=binding.tvAddCard
        val tvTaskListTitle=binding.tvTaskListTitle
        val ibCloseListName=binding.ibCloseListName
        val ibDoneListName=binding.ibDoneListName
        val etTaskListName=binding.etTaskListName
        val ibEditListName=binding.ibEditListName
        val ibDeleteList=binding.ibDeleteList
        val etEditTaskListName=binding.etEditTaskListName
        val ibCloseEditableView=binding.ibCloseEditableView
        val ibDoneEditListName=binding.ibDoneEditListName
        val ibCloseCardName=binding.ibCloseCardName
        val ibDoneCardName=binding.ibDoneCardName
        val etCardName=binding.etCardName
        val rvCardList=binding.rvCardList
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TaskListItemAdapter.ViewHolder {
        val viewBinding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context),
        parent,
        false)

        //this is for adjusting width of the linear layout, we want it to cover 70% of the screen
        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT) //width,height

        layoutParams.setMargins((15.toDp().toPx()),0,(40.toDp().toPx()),0)
        viewBinding.root.layoutParams=layoutParams

        return ViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model=list[position]

        if (position == list.size-1){
            holder.tvAddTaskList.visibility=View.VISIBLE
            holder.llTaskItem.visibility=View.GONE
        }else{
            holder.tvAddTaskList.visibility=View.GONE
            holder.llTaskItem.visibility=View.VISIBLE
        }

        holder.tvTaskListTitle.text=model.title

        holder.tvAddTaskList.setOnClickListener {
            holder.tvAddTaskList.visibility=View.GONE
            holder.cvAddTaskListName.visibility=View.VISIBLE
        }

        holder.ibCloseListName.setOnClickListener {
            holder.tvAddTaskList.visibility=View.VISIBLE
            holder.cvAddTaskListName.visibility=View.GONE
        }
        holder.ibDoneListName.setOnClickListener {
            val listName = holder.etTaskListName.text.toString()

            if (listName.isNotEmpty()){
                if (context is TaskListActivity){
                    context.createTaskList(listName)
                }
            }else{
                Toast.makeText(context,"Task List name cannot be empty",Toast.LENGTH_SHORT).show()
            }
        }

        holder.ibEditListName.setOnClickListener {
            holder.etEditTaskListName.setText(model.title)
            holder.llTitleView.visibility=View.GONE
            holder.cvEditTaskListName.visibility=View.VISIBLE
        }
        holder.ibCloseEditableView.setOnClickListener {
            holder.llTitleView.visibility=View.VISIBLE
            holder.cvEditTaskListName.visibility=View.GONE
        }

        //Update list name
        holder.ibDoneEditListName.setOnClickListener {
            val listName=holder.etEditTaskListName.text.toString()

            if (listName.isNotEmpty()){
                if (context is TaskListActivity){
                    context.updateTaskList(position,listName,model)
                }
            }else{
                Toast.makeText(context,"Task List name cannot be empty",Toast.LENGTH_SHORT).show()
            }
        }
        holder.ibDeleteList.setOnClickListener {
            if (context is TaskListActivity){
                context.deleteTaskList(position)
            }
        }
        holder.tvAddCard.setOnClickListener {
            holder.tvAddCard.visibility=View.GONE
            holder.cvAddCard.visibility=View.VISIBLE
        }
        holder.ibCloseCardName.setOnClickListener{
            holder.tvAddCard.visibility=View.VISIBLE
            holder.cvAddCard.visibility=View.GONE
        }

        //for CARD
        holder.ibDoneCardName.setOnClickListener {
            val cardName=holder.etCardName.text.toString()

            if (cardName.isNotEmpty()){
                if (context is TaskListActivity){
                    context.addCardToTaskList(position,cardName)
                }
            }else{
                Toast.makeText(context,"Card name cannot be empty",Toast.LENGTH_SHORT).show()
            }
        }

        //For card recycler view
        holder.rvCardList.layoutManager=LinearLayoutManager(context)
        holder.rvCardList.setHasFixedSize(true)
        val adapter=CardListItemAdapter(context,model.cards)
        holder.rvCardList.adapter=adapter

        //set onclick for each card in this tasklist
        adapter.setOnClickListener(
            object :CardListItemAdapter.OnClickListener{
                override fun OnClick(cardPosition: Int) {
                    if (context is TaskListActivity){
                        context.cardDetails(position,cardPosition) //this will startActivity
                    }
                }
            }
        )

        //For drag and drop rearrange card functionality
        val dividerItemDecoration=DividerItemDecoration(context,DividerItemDecoration.VERTICAL)
        holder.rvCardList.addItemDecoration(dividerItemDecoration)

        val touchHelper=ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,0){
            override fun onMove(
                recyclerView: RecyclerView,
                draggedFrom: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val draggedFromPos=draggedFrom.adapterPosition
                val targetPosition=target.adapterPosition

                if (mPositionDraggedFrom==-1){
                    mPositionDraggedFrom=draggedFromPos
                }
                mPositionDraggedTo=targetPosition

                Collections.swap(list[position].cards,draggedFromPos,targetPosition)
                adapter.notifyItemMoved(draggedFromPos,targetPosition)
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            //called when dragging and dropping is over
            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                if (mPositionDraggedFrom!=-1 && mPositionDraggedTo!=-1 && mPositionDraggedFrom!=mPositionDraggedTo){
                    (context as TaskListActivity).updateCardsInTaskListOnDrag(
                        position,list[position].cards)
                }
                mPositionDraggedFrom=-1
                mPositionDraggedTo=-1
            }

        })
        touchHelper.attachToRecyclerView(holder.rvCardList)
    }

    override fun getItemCount(): Int {
        return list.size
    }



    //To convert dp into int for scaling our views
    private fun Int.toDp():Int =
        (this/ Resources.getSystem().displayMetrics.density).toInt() //divide

    private fun Int.toPx():Int =
        (this* Resources.getSystem().displayMetrics.density).toInt() //multiply
}