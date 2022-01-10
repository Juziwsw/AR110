package com.hiar.ar110.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.Utils
import com.hiar.ar110.R
import com.hiar.ar110.data.people.FaceCompareBaseInfo
import com.hiar.ar110.extension.gone
import com.hiar.ar110.extension.setOnDoubleClickListener
import com.hiar.ar110.extension.showAlertAnimation
import com.hiar.ar110.extension.visible
import com.hiar.ar110.listener.OnImageItemClickListener
import com.hiar.ar110.util.Util
import java.util.*


/**
 * Author:wilson.chen
 * date：6/17/21
 * desc：
 */
class PopulationPeopleListAdapter(val onImageItemClickListener: OnImageItemClickListener, val mPeopleList: MutableList<MutableList<FaceCompareBaseInfo>>) : RecyclerView.Adapter<PopulationPeopleListAdapter.ViewHolder>() {
    val dp = Utils.getApp().resources.displayMetrics.density
    val currentDate = TimeUtils.millis2Date(System.currentTimeMillis())
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_population_recognized_layout, parent, false))
    }

    override fun getItemCount(): Int = mPeopleList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val position = holder.adapterPosition
        val peoInfo = mPeopleList[position] ?: return
        if (peoInfo.isNotEmpty()) {
            val messageInfo = peoInfo[0]
            messageInfo.apply {
                holder.textPeoName.text = StringUtils.null2Length0(faceName)
                holder.tv_phone.text = StringUtils.null2Length0(phone)

                holder.textId.text = StringUtils.null2Length0(cardId)
                holder.tv_validity_start.text = StringUtils.null2Length0(inTime).run {
                    if (this.length > 1) {
                        this.substring(0, "2021-01-27".length)
                    } else {
                        this
                    }
                }
                holder.tv_validity_end.text = StringUtils.null2Length0(endTime).run {
                    if (this.length > 9) {
                        val end = TimeUtils.string2Date(endTime, "yyyy-MM-dd HH:mm")
                        holder.tv_validity_end.isActivated = currentDate.after(end)
                        this.substring(0, "2021-01-27".length)
                    } else {
                        this
                    }
                }

                holder.tv_company.text = StringUtils.null2Length0(fwcs)
                holder.tv_address.text = StringUtils.null2Length0(fwdz)
                holder.tv_company_phone.text = StringUtils.null2Length0(dwdh)

                holder.tv_home_address.text = StringUtils.null2Length0(jzdz)
                holder.tv_house_owner.text = StringUtils.null2Length0(fzxm)
                holder.tv_house_owner_phone.text = StringUtils.null2Length0(fzphone)

                val tag1 = getTagView(holder.itemView.context).apply {
                    text = labelName
                }
                if (labelCode >= 3){
                    tag1.isActivated =true
                    holder.iv_alert.visible()
                    holder.iv_alert.showAlertAnimation()
                }else{
                    holder.iv_alert.gone()
                    holder.iv_alert.animation?.cancel()
                    tag1.isActivated =false
                }
                holder.ll_tag_parent.removeAllViews()
                holder.ll_tag_parent.addView(tag1)

                holder.tv_home_address.apply {
                    setOnDoubleClickListener {
                        Util.showMessage(text.toString())
                    }
                }
                holder.tv_address.apply {
                    setOnDoubleClickListener {
                        Util.showMessage(text.toString())
                    }
                }
                holder.tv_company.apply {
                    setOnDoubleClickListener {
                        Util.showMessage(text.toString())
                    }
                }
            }

            val mFaceList = ArrayList<FaceCompareBaseInfo>()
            for (j in peoInfo.indices) {
                mFaceList.add(peoInfo[j])
            }
            if (mFaceList.size > 0) {
                holder.mFaceAdapter.setAdapter(mFaceList)
            }
        }
    }

    /**
     * 新增识别的数据
     */
    fun addNewFace(holder: RecyclerView.ViewHolder?, index: Int, it: MutableList<FaceCompareBaseInfo>) {
        if (holder is ViewHolder) {
            val data = mPeopleList.get(index)
            data.addAll(0, it)
            holder.mFaceAdapter.addNewData(it)
            if (index != 0) {
                Collections.swap(mPeopleList, index, 0)
                notifyItemMoved(index, 0)
            }
            holder.recyclerView.scrollToPosition(0)
        }
    }

    fun resetAdapter() {
        mPeopleList.clear()
        notifyDataSetChanged()
    }

    fun setAdapter(content: MutableList<MutableList<FaceCompareBaseInfo>>?) {
        if (content.isNullOrEmpty()) {
            return
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textPeoName: TextView = itemView.findViewById(R.id.tv_name)
        val textId: TextView = itemView.findViewById(R.id.tv_id)
        val tv_phone: TextView = itemView.findViewById(R.id.tv_phone)
        val tv_validity_start: TextView = itemView.findViewById(R.id.tv_validity_start)
        val tv_validity_end: TextView = itemView.findViewById(R.id.tv_validity_end)
        val tv_company: TextView = itemView.findViewById(R.id.tv_company)
        val tv_address: TextView = itemView.findViewById(R.id.tv_address)
        val tv_company_phone: TextView = itemView.findViewById(R.id.tv_company_phone)
        val tv_home_address: TextView = itemView.findViewById(R.id.tv_home_address)
        val tv_house_owner: TextView = itemView.findViewById(R.id.tv_house_owner)
        val tv_house_owner_phone: TextView = itemView.findViewById(R.id.tv_house_owner_phone)
        val iv_alert: ImageView = itemView.findViewById(R.id.iv_alert)
        val ll_tag_parent: LinearLayout = itemView.findViewById(R.id.ll_tag_parent)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recycler_peop_compare)
        var mFaceAdapter: PeoCompFaceAdapter

        init {
            recyclerView.layoutManager = LinearLayoutManager(itemView.context, RecyclerView.HORIZONTAL, false)
            mFaceAdapter = PeoCompFaceAdapter(itemView.context.applicationContext, onImageItemClickListener)
            recyclerView.adapter = mFaceAdapter
        }
    }

    @SuppressLint("RestrictedApi")
    private fun getTagView(context: Context): TextView {
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.marginEnd = (8 * dp).toInt()
        return TextView(context).apply {
            layoutParams = params
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val tf = Utils.getApp().resources.getFont(R.font.pingfang_sc_semibold)
                typeface = tf
            }
            setBackgroundResource(R.drawable.selector_bg_population_tag)
            setTextColor(Utils.getApp().resources.getColorStateList(R.color.tag_color, null))
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12.0f)
            setPadding((8 * dp).toInt(), (3 * dp).toInt(), (8 * dp).toInt(), (3 * dp).toInt())
        }
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        holder.iv_alert.animation?.cancel()
        super.onViewDetachedFromWindow(holder)
    }
}