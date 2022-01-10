package com.hiar.ar110.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.hiar.ar110.R;

public class ProgressDialog extends AlertDialog {
    private ProgressBar progressBar;
    private TextView tvProgress;
    private int maxProgress = 100;

    public ProgressDialog(@NonNull Context context) {
        super(context);
        progressBar = (ProgressBar) LayoutInflater.from(context).inflate(R.layout.dialog_horizontal_progress_bar, null);
        progressBar.setMax(maxProgress);
        tvProgress = new TextView(context);
        tvProgress.setText("");
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(tvProgress);
        linearLayout.addView(progressBar);
        setView(linearLayout, 4, 4, 4, 4);
        setCanceledOnTouchOutside(false);
        setTitle(R.string.file_upload_notice);
    }


    public void setMaxProgress(int max) {
        if (max > 0) {
            this.maxProgress = max;
            if (progressBar != null) {
                progressBar.setMax(max);
                tvProgress.setText("开始上传1 / " + max);
            }
        }
    }

    public void updateUploadNum(int current) {
        if(null != tvProgress) {
            String strProgress = String.format("当前进度:%d / %d", current, maxProgress);
            tvProgress.setText(strProgress);
        }
    }


    @SuppressLint("StringFormatInvalid")
    public void refreshProgress(int progress, String filename) {
        if (progressBar != null) {
            progressBar.setProgress(progress);
        }
        if(null != tvProgress) {
            String strProgress = String.format("上传进度: %d/%d，%s", progress, maxProgress, filename);
            tvProgress.setText(strProgress);
        }
    }
}
