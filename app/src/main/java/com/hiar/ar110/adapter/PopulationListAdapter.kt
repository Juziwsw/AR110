package com.hiar.ar110.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.hiar.ar110.R
import com.hiar.ar110.data.cop.PatrolRecord
import com.hiar.ar110.data.people.PopulationRecord
import com.hiar.ar110.extension.setOnThrottledClickListener

/**
 * Author:wilson.chen
 * date：5/25/21
 * desc：
 */
class PopulationListAdapter(
        val mDatas: MutableList<PopulationRecord>,
        val itemClick: (position:Int) -> Unit,
) : RecyclerView.Adapter<PopulationListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_patrol_item, parent, false))
    }

    override fun getItemCount(): Int {
        return mDatas.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = mDatas[position]
        holder.textStartTime.setText(record.startTime)
        holder.textEndTime.setText(record.endTime)
        holder.patrolNum.setText(record.number)
        if (record.status == 1) {
            holder.status.setImageResource(R.drawable.bq_home_completed)
        } else {
            holder.status.setImageResource(R.drawable.ic_bq_home_ing)
        }
        holder.mRelaItem.setOnThrottledClickListener {
            itemClick.invoke(position)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var patrolNum: TextView = itemView.findViewById(R.id.text_task_number_val)
        var textStartTime: TextView = itemView.findViewById(R.id.text_start_time_val)
        var textEndTime: TextView = itemView.findViewById(R.id.text_endtime_val)
        var status: ImageView = itemView.findViewById(R.id.img_status)
        var mRelaItem: ConstraintLayout = itemView.findViewById(R.id.layout_patrol_base)
    }
}