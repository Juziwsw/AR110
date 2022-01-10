package com.hiar.ar110.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.hiar.ar110.R
import com.hiar.ar110.data.vehicle.VehicleRecogList
import com.hiar.ar110.listener.OnImageItemClickListener
import com.hiar.ar110.util.Util
import java.util.*

class VehicleListAdapter(private val mContext: Context, private val cjdbj2: String, private val mImageItemClickListener: OnImageItemClickListener?) : RecyclerView.Adapter<VehicleListAdapter.ViewHolder>() {
    private val mCarList: ArrayList<VehicleRecogList>? = ArrayList()
    fun setAdapter(content: List<VehicleRecogList>?) {
        if (null == content) {
            return
        }
        mCarList!!.clear()
        mCarList.addAll(content)
        notifyDataSetChanged()
    }

    fun resetAdapter() {
        mCarList!!.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.car_record_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        val position = holder.adapterPosition
        val vInfo = mCarList!![position] ?: return
        val plateNum = vInfo.carId
        if (holder.plateNumber != null) {
            if (!TextUtils.isEmpty(plateNum)) {
                holder.plateNumber!!.text = plateNum
            } else {
                holder.plateNumber!!.text = ""
            }
        }
        if (vInfo.recoStatus == 1) {
            return
        }
        if (vInfo.recoRecord != null) {
            val imgViews = arrayOfNulls<AppCompatImageView>(4)
            val textViews = arrayOfNulls<TextView>(4)
            imgViews[0] = holder.carCrop1
            imgViews[1] = holder.carCrop2
            imgViews[2] = holder.carCrop3
            imgViews[3] = holder.carCrop4
            textViews[0] = holder.textCrop1
            textViews[1] = holder.textCrop2
            textViews[2] = holder.textCrop3
            textViews[3] = holder.textCrop4
            var len = vInfo.recoRecord.size
            len = if (len >= 4) 4 else len
            var i = 0
            while (i < len) {
                if (!TextUtils.isEmpty(vInfo.recoRecord[i].url)) {
                    val url = Util.getCarPhotoIpHead() + vInfo.recoRecord[i].url
                    Glide.with(mContext).load(url).diskCacheStrategy(DiskCacheStrategy.NONE).into(imgViews[i])
                    imgViews[i]!!.setOnClickListener {  mImageItemClickListener?.onItemClick(url) }
                }
                val gpsTime = vInfo.recoRecord[i].gpsTime
                if (!TextUtils.isEmpty(gpsTime)) {
                    val text = gpsTime.substring(5)
                    textViews[i]!!.text = text
                }
                i++
            }
            for (j in i..3) {
                imgViews[j]!!.setImageDrawable(null)
            }
        }
        if (!TextUtils.isEmpty(vInfo.carColor)) {
            holder.carColor.text = vInfo.carColor
        } else {
            holder.carColor.text = ""
        }
        if (!TextUtils.isEmpty(vInfo.carBrand)) {
            holder.textBrand.text = vInfo.carBrand
        } else {
            holder.textBrand.text = ""
        }
        holder.legalName.text = ""
        holder.textPhone.text = ""
        if (!TextUtils.isEmpty(vInfo.legalPersonName)) {
            holder.legalName.text = vInfo.legalPersonName
        }
        if (!TextUtils.isEmpty(vInfo.driverPhone)) {
            holder.textPhone.text = vInfo.driverPhone
        }
        if (!TextUtils.isEmpty(vInfo.carType)) {
            holder.carType.text = vInfo.carType
        } else {
            holder.carType.text = ""
        }
        if (!TextUtils.isEmpty(vInfo.ownerAddress)) {
            holder.ownerAddress.text = vInfo.ownerAddress
        } else {
            holder.ownerAddress.text = ""
        }
        if (!TextUtils.isEmpty(vInfo.driverId)) {
            holder.legalId.text = vInfo.driverId
        } else {
            holder.legalId.text = ""
        }
    }

    override fun getItemCount(): Int {
        return mCarList?.size ?: 0
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var carCrop1: AppCompatImageView= itemView.findViewById(R.id.img_car_crop1)
        var carCrop2: AppCompatImageView = itemView.findViewById(R.id.img_car_crop2)
        var carCrop3: AppCompatImageView= itemView.findViewById(R.id.img_car_crop3)
        var carCrop4: AppCompatImageView= itemView.findViewById(R.id.img_car_crop4)
        var plateNumber: TextView? = itemView.findViewById(R.id.text_platenum)
        var textBrand: TextView = itemView.findViewById(R.id.text_brand)
        var carType: TextView= itemView.findViewById(R.id.text_car_type)
        var carColor: TextView = itemView.findViewById(R.id.text_car_color)
        var legalName: TextView= itemView.findViewById(R.id.text_legal_people)
        var legalId: TextView= itemView.findViewById(R.id.text_legal_idcard)
        var ownerAddress: TextView = itemView.findViewById(R.id.text_car_address)
        var carTag: TextView = itemView.findViewById(R.id.text_criminal_type)
        var textCrop1: TextView= itemView.findViewById(R.id.text_car_crop1)
        var textCrop2: TextView= itemView.findViewById(R.id.text_car_crop2)
        var textCrop3: TextView = itemView.findViewById(R.id.text_car_crop3)
        var textCrop4: TextView= itemView.findViewById(R.id.text_car_crop4)
        var textPhone: TextView = itemView.findViewById(R.id.text_legal_phone)

    }
}