package com.hiar.ar110.util

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.TextView
import com.hiar.ar110.R
import com.hiar.ar110.extension.setOnThrottledClickListener

/**
 * Author:wilson.chen
 * date：6/29/21
 * desc：
 */
fun showConfirmDialog(context: Context, title: String, confirmClick: () -> Unit) {
    val alertDialogBuilder = AlertDialog.Builder(context)
    val view = View.inflate(context, R.layout.layout_mydialog, null)
    alertDialogBuilder.setView(view)
    val alertDialog = alertDialogBuilder.create()
    alertDialog.show()
    alertDialog.setCancelable(false)
    val width = context.resources.getDimension(R.dimen.dp320).toInt()
    val height = context.resources.getDimension(R.dimen.dp166).toInt()
    alertDialog.getWindow()?.setLayout(width, height)
    val mTextNo = view.findViewById<TextView>(R.id.text_cancel)
    val text_task_title = view.findViewById<TextView>(R.id.text_task_title)
    text_task_title.text = title
    mTextNo.setOnThrottledClickListener { v: View? -> alertDialog.dismiss() }
    val mTextYes = view.findViewById<TextView>(R.id.text_ok)
    mTextYes.setOnThrottledClickListener {
        alertDialog.dismiss()
        confirmClick.invoke()
    }
}