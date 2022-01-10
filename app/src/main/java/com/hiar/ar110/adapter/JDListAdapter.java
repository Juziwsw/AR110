package com.hiar.ar110.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hiar.ar110.R;
import com.hiar.ar110.data.cop.CopTaskRecord;
import com.hiar.ar110.helper.NavigationHelper;
import com.hiar.ar110.service.AR110BaseService;
import com.hiar.ar110.util.Util;
import com.hiar.ar110.viewmodel.JDListViewModel;
import com.hiar.mybaselib.utils.AR110Log;
import com.hiar.mybaselib.utils.KeyUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class JDListAdapter extends RecyclerView.Adapter<JDListAdapter.ViewHolder>{
    private List<CopTaskRecord> mDatas = new ArrayList<>();
    private Activity mActivity;
    public int mCurrentSel = 0;

    public JDListAdapter(Activity act) {
        mActivity = act;
    }

    public void setAdapter(List<CopTaskRecord> content) {
        if(null == content) {
            return;
        }

        mDatas.clear();
        mDatas.addAll(content);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_jdlist_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        int position = holder.getAdapterPosition();
        if(mDatas.get(position) == null) {
            return;
        }

        holder.time.setText(mDatas.get(position).bjsj);
        holder.content.setText(mDatas.get(position).bjnr);
        holder.loccation.setText(mDatas.get(position).afdd);
        holder.cjd_num.setText(mDatas.get(position).cjdbh2);
        AR110Log.i("jdlist","position="+position+",cjdbh2="+mDatas.get(position).cjdbh2+",status="+mDatas.get(position).cjzt);
        if(mDatas.get(position).cjzt2 < Util.CJZT_CJZ) {
            holder.status.setImageResource(R.drawable.bq_home_unprocessed);
        } else if(mDatas.get(position).cjzt2 < Util.CJZT_YWC) {
            holder.status.setImageResource(R.drawable.ic_bq_home_ing);
        } else {
            holder.status.setImageResource(R.drawable.bq_home_completed);
        }

        KeyUtil.preventRepeatedClick(holder.mRelaItem, view -> {
            if(!AR110BaseService.Companion.isInitialized()) {
                return;
            }

            if(JDListViewModel.mIsLoading) {
                Util.showMessage("正在刷新警单，请稍后...");
                return;
            }

            NavigationHelper.Companion.getInstance().beginHandleJD(mDatas.get(position), null);
        });
    }

    @Override
    public int getItemCount() {
        return null==mDatas?0:mDatas.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView time;
        TextView cjd_num;
        TextView loccation;
        TextView content;
        ImageView status;
        RelativeLayout mRelaItem;

        public ViewHolder(View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.text_time_val);
            cjd_num = itemView.findViewById(R.id.text_jdnum_value);
            loccation = itemView.findViewById(R.id.text_loc_val);
            content = itemView.findViewById(R.id.text_content_val);
            status = itemView.findViewById(R.id.img_status);
            mRelaItem = itemView.findViewById(R.id.layout_adapter);
        }
    }
}