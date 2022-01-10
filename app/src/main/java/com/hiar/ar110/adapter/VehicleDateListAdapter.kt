package com.hiar.ar110.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hiar.ar110.R
import com.hiar.ar110.fragment.VehicleRecordFragment
import com.hiar.mybaselib.utils.KeyUtil
import java.util.*

class VehicleDateListAdapter(private val mFragMent: VehicleRecordFragment) : RecyclerView.Adapter<VehicleDateListAdapter.ViewHolder>() {
    private val mDateData: ArrayList<String>? = ArrayList()
    fun setAdapter(content: List<String>?) {
        if (null == content) {
            return
        }
        mDateData!!.clear()
        mDateData.addAll(content)
        notifyDataSetChanged()
    }

    fun resetAdapter() {
        mDateData!!.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.vehicle_date_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        val position = holder.adapterPosition
        holder.mTextDate.text = mDateData!![position]
        KeyUtil.preventRepeatedClick(holder.mTextDate) { view: View? -> mFragMent.setNewDate(mDateData[position]) }
    }

    override fun getItemCount(): Int {
        return mDateData?.size ?: 0
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mTextDate: TextView

        init {
            mTextDate = itemView.findViewById(R.id.text_date_content)
        }
    }
}