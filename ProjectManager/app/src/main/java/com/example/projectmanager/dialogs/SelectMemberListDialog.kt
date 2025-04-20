package com.example.projectmanager.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectmanager.R
import com.example.projectmanager.adapters.MemberItemsAdapter
import com.example.projectmanager.models.User

abstract class SelectMemberListDialog(context: Context, private var list: ArrayList<User>,
                                      private var title:String=""): Dialog(context) {

    private var tvSelectMemberTitle:TextView?=null
    private var rvSelectMemberList:RecyclerView?=null
    private var adapter: MemberItemsAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view= LayoutInflater.from(context).inflate(R.layout.dialog_select_member_list,null)
        setContentView(view)

        tvSelectMemberTitle=findViewById(R.id.tvSelectMemberTitle)
        rvSelectMemberList=findViewById(R.id.rvSelectMemberList)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setupRecyclerView()
    }

    private fun setupRecyclerView(){
        if (list.size>0){
            tvSelectMemberTitle?.text=title
            rvSelectMemberList?.layoutManager= LinearLayoutManager(context)

            adapter= MemberItemsAdapter(context,list)

            rvSelectMemberList?.adapter=adapter

            adapter!!.setOnClickListener(object :MemberItemsAdapter.OnClickListener{
                override fun OnClick(position: Int, user: User, action: String) {
                    dismiss()
                    onMemberSelected(user,action)
                }
            })
        }
    }

    protected abstract fun onMemberSelected(user:User,action: String)

}