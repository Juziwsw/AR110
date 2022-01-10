package com.hiar.ar110.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hiar.ar110.ConstantApp;
import com.hiar.ar110.R;
import com.hiar.ar110.adapter.MsgListAdapter;
import com.hiar.ar110.base.BaseFragment;
import com.hiar.ar110.helper.NavigationHelper;
import com.hiar.ar110.viewmodel.RecentContactsViewModel;
import com.hiar.ar110.widget.RecyclerViewMultiClickListener;
import com.hiar.mybaselib.utils.AR110Log;
import com.hileia.common.entity.proto.EntityOuterClass;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * author: liwf
 * date: 2021/3/23 16:27
 * 会话列表（包括群组及个人会话）
 */
public class RecentMsgFragment extends BaseFragment {
    private ImageView imgBack;
    private TextView tvTitle, tvAdd;
    private RecentContactsViewModel mViewModel;
    private MsgListAdapter mMsgListAdapter;
    private RecyclerView mRvMsg;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_recent_msg;
    }

    @Override
    public void initData() {
        mViewModel = getViewModel(RecentContactsViewModel.class);
    }

    @Override
    public void initView(View view) {
        imgBack = view.findViewById(R.id.img_back);
        tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(getResources().getString(R.string.recent_msg_list));
        tvAdd = view.findViewById(R.id.tv_detail);
        mRvMsg = view.findViewById(R.id.fragment_msg_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRvMsg.setLayoutManager(linearLayoutManager);
        mMsgListAdapter = new MsgListAdapter(getContext());
        mRvMsg.setAdapter(mMsgListAdapter);
        mRvMsg.setHasFixedSize(true);
        mRvMsg.setItemViewCacheSize(20);
    }

    @Override
    public void initListener() {
        imgBack.setOnClickListener(v -> {
            back();
        });
        tvAdd.setOnClickListener(v -> {
            Activity act = getActivity();
            if (act != null) {

            }
        });
        mRvMsg.addOnItemTouchListener(new RecyclerViewMultiClickListener(getContext(), mRvMsg, new RecyclerViewMultiClickListener.SimpleOnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                EntityOuterClass.Entity.RecentContactInfo info = mMsgListAdapter.messageList.get(position);
                Bundle bundle = new Bundle();
                bundle.putString(ConstantApp.CHAT_RC_ID_KEY, info.getRcid());
                bundle.putInt(ConstantApp.CHAT_TYPE_KEY, info.getRctype());
                NavigationHelper.Companion.getInstance().beginChat(bundle, null);
            }
        }));
        mViewModel.getRecentContact().observe(this, recentContactList -> {
            mMsgListAdapter.messageList.clear();
            for (int index = 0; index < recentContactList.getListCount(); index++) {
                mMsgListAdapter.messageList.add(recentContactList.getList(index));
            }
            refreshView();
        });
        mViewModel.getNewMsg().observe(this, aBoolean -> {
            if (aBoolean) {
                loadRecentData();
            }
        });
    }

    @Override
    public void requestData() {
        loadRecentData();
    }

    private void loadRecentData() {
        EntityOuterClass.Entity.RecentContactList recentContacts = mViewModel.getRecentContacts();
        AR110Log.i(getTAG(), "recentContacts: %d", recentContacts.getListCount());
        if (recentContacts.getListCount() <= 0) return;
        mMsgListAdapter.messageList.clear();
        for (int index = 0; index < recentContacts.getListCount(); index++) {
            mMsgListAdapter.messageList.add(recentContacts.getList(index));
        }
        refreshView();
    }

    private void refreshView() {
        if (mMsgListAdapter.messageList.size() > 0) {
            mMsgListAdapter.notifyDataSetChanged();
        }
    }

    private void back() {
        Activity act = getActivity();
        if (act != null) {
            NavigationHelper.Companion.getInstance().backToHomePage(this);
        }
    }

    @Override
    public void onBackPressed() {
        back();
//        return true;
    }
}
