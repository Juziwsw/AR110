package com.hiar.ar110.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.hiar.ar110.R
import com.hiar.ar110.data.MyVideoInfo
import java.io.File

/**
 * Author:wilson.chen
 * date：5/26/21
 * desc：
 */
class VideoWallAdapter(val mContext: Context, val mPhotoWall: GridView, val mVideoList: MutableList<MyVideoInfo>) : BaseAdapter() {

    private inner class ViewHolder {
        var mThumbNails: ImageView? = null
        var mImgVlogo: ImageView? = null
        var mImgStatus: ImageView? = null
        var mTextView: TextView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var convertView = convertView
        val info: MyVideoInfo = mVideoList.get(position)
        val url = info.mVideoAbsName
        val holder: ViewHolder
        if (convertView == null) {
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.item_grid, parent, false)
            holder = ViewHolder()
            holder.mTextView = convertView!!.findViewById(R.id.text_file_name)
            holder.mThumbNails = convertView.findViewById(R.id.img_video)
            holder.mImgStatus = convertView.findViewById(R.id.img_status)
            holder.mImgVlogo = convertView.findViewById(R.id.video_logo)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        val videoFile = File(url)
        if (videoFile.exists()) {
            Glide.with(mContext.getApplicationContext()).load(Uri.fromFile(videoFile))
                    .diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.mThumbNails)
            val fileName = videoFile.name
            holder.mTextView!!.text = fileName.substring(0, fileName.length - 4)
            holder.mImgStatus!!.setImageResource(mUploadStateRes[info.upLoadState])
        }
        return convertView
    }

    override fun getCount(): Int {
        return mVideoList.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    companion object {
        val mUploadStateRes = intArrayOf(R.drawable.icon_not_upload, R.drawable.icon_uploading, R.drawable.icon_upload_ok)
    }
}