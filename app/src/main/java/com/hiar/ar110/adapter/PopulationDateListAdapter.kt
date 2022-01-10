package com.hiar.ar110.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hiar.ar110.R
import com.hiar.ar110.extension.setOnThrottledClickListener

/**
 * Author:wilson.chen
 * date：5/25/21
 * desc：
 */
class PopulationDateListAdapter(
        val mDateData: MutableList<String>,
        val itemClick: (position:Int) -> Unit,
) : RecyclerView.Adapter<PopulationDateListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.vehicle_date_list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return mDateData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val position = holder.adapterPosition
        holder.mTextDate.text = mDateData[position]
        holder.itemView.setOnThrottledClickListener {
            itemClick.invoke(position)
        }
    }
    fun resetAdapter() {
        mDateData.clear()
        notifyDataSetChanged()
    }

    fun setAdapter(content: MutableList<String>?) {
        if (content.isNullOrEmpty()) {
            return
        }
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mTextDate: TextView = itemView.findViewById(R.id.text_date_content)
    }
}