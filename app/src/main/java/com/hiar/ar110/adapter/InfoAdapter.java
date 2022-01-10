//package com.hiar.ar110.adapter;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//
//import com.hiar.ar110.R;
//import org.xutils.common.util.KeyValue;
//import java.util.ArrayList;
//import java.util.List;
//
//public class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.ViewHolder>{
//    private List<KeyValue> mDatas = new ArrayList<>();
//
//    public void setAdapter(List<KeyValue> content) {
//        if(null == content) {
//            return;
//        }
//
//        mDatas.clear();
//        mDatas.addAll(content);
//        notifyDataSetChanged();
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_info,parent,false));
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
//        int position = holder.getAdapterPosition();
//        KeyValue keyValue = mDatas.get(position);
//        holder.title.setText(keyValue.key);
//        holder.value.setText((String)keyValue.value);
//        if(position == mDatas.size() - 1) {
//            holder.value.setNextFocusDownId(holder.value.getId());
//        }
//    }
//
//    public void setData(){
//        mDatas = new ArrayList<>();
//        mDatas.add(new KeyValue("姓名","二麻子"));
//        mDatas.add(new KeyValue("民族","汉族"));
//        mDatas.add(new KeyValue("身份证","11987"));
//        mDatas.add(new KeyValue("户籍地","北仑区"));
//        notifyDataSetChanged();
//    }
//
//    @Override
//    public int getItemCount() {
//        return null==mDatas?0:mDatas.size();
//    }
//
//    class ViewHolder extends RecyclerView.ViewHolder{
//        TextView title;
//        TextView value;
//
//        public ViewHolder(View itemView) {
//            super(itemView);
//            title = itemView.findViewById(R.id.info_title);
//            value = itemView.findViewById(R.id.info_value);
//        }
//    }
//}
