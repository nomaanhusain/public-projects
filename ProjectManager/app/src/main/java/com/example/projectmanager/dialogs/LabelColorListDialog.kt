package com.example.projectmanager.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectmanager.R
import com.example.projectmanager.adapters.LabelColorListItemAdapter

abstract class LabelColorListDialog(
    context:Context,
    private var list: ArrayList<String>,
    private var title:String="",
    private var mSelectedColor:String=""
):Dialog(context) {

    private var adapter:LabelColorListItemAdapter?=null
    private var tvTitle:TextView?=null
    private var rvList:RecyclerView?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view=LayoutInflater.from(context).inflate(R.layout.dialog_select_color_list,null)
        setContentView(view)

        tvTitle=findViewById(R.id.tvTitle)
        rvList=findViewById(R.id.rvList)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setupRecyclerView()
    }

    private fun setupRecyclerView(){
        tvTitle?.text=title
        rvList?.layoutManager=LinearLayoutManager(context)
        adapter= LabelColorListItemAdapter(context,list,mSelectedColor)

        rvList?.adapter=adapter

        adapter!!.onItemClickListener = object :LabelColorListItemAdapter.OnItemClickListener{
            override fun OnClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)
            }
        }
    }

    protected abstract fun onItemSelected(color: String)
}