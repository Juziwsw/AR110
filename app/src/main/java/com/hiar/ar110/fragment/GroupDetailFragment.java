package com.hiar.ar110.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hiar.ar110.ConstantApp;
import com.hiar.ar110.R;
import com.hiar.ar110.adapter.GroupDetailAdapter;
import com.hiar.ar110.base.BaseFragment;
import com.hiar.ar110.helper.NavigationHelper;
import com.hiar.ar110.viewmodel.GroupViewModel;
import com.hiar.mybaselib.utils.EmoticonsKeyboardUtils;
import com.hiar.mybaselib.utils.toast.ToastUtils;
import com.hileia.common.entity.proto.EntityOuterClass;
import com.hileia.common.entity.proto.Enums;

import java.util.List;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * author: liwf
 * date: 2021/3/19 11:36
 * 群组详情界面
 */
public class GroupDetailFragment extends BaseFragment {

    private ImageView imgBack;
    private TextView tvName, tvAdd, tvNumber;
    private GroupViewModel mViewModel;
    private String mGroupId;
    private GroupDetailAdapter mAdapter;
    private RecyclerView mMemberListView;
    private String mGroupName;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_group_detail;
    }

    @Override
    public void initData() {
        mGroupId = getArguments().getString(ConstantApp.CHAT_GROUP_ID_KEY);
        mViewModel = getViewModel(GroupViewModel.class);
        mViewModel.setGroupId(mGroupId);
    }

    @Override
    public void initView(View view) {
        imgBack = view.findViewById(R.id.img_back);
        tvNumber = view.findViewById(R.id.tv_number);
        tvAdd = view.findViewById(R.id.tv_add);
        tvName = view.findViewById(R.id.tv_name);
        mMemberListView = view.findViewById(R.id.rv_member_list);
        GridLayoutManager linearLayoutManager = new GridLayoutManager(getContext(), 5);
        mMemberListView.setLayoutManager(linearLayoutManager);
        mAdapter = new GroupDetailAdapter(R.layout.item_group_member,
                getContext().getApplicationContext());
        mMemberListView.setAdapter(mAdapter);
        mMemberListView.setHasFixedSize(true);
    }

    @Override
    public void initListener() {
        imgBack.setOnClickListener(v -> {
            back();
        });
        tvAdd.setOnClickListener(v -> {
            Activity act = getActivity();
            if (act != null) {
                NavigationHelper.Companion.getInstance().beginGroupManager(mGroupId, this);
            }
        });
        tvName.setOnClickListener(v -> showEditDialog());
        mViewModel.getGroupUpdateLiveData().observe(this, groupInfoReqResult -> {
            if (groupInfoReqResult.getStatus() == ConstantApp.STATUS_SUCCESS) {
                ToastUtils.show("更新成功");
            } else {
                ToastUtils.show("更新失败");
            }
            EmoticonsKeyboardUtils.closeSoftKeyboard(getContext());
        });
        mViewModel.getLastUpdateLiveData().observe(this, s -> requestData());
    }

    @Override
    public void requestData() {
        List<EntityOuterClass.Entity.ContactInfo> contactInfoList =
                mViewModel.getGroupMembers(mGroupId);
        tvNumber.setText(String.format("%d 人", contactInfoList.size()));
        mAdapter.getData().clear();
        mAdapter.addData(0, contactInfoList);
        mGroupName = mViewModel.getGroupInfo(mGroupId).getName();
        tvName.setText(mGroupName);
    }

    private void back() {
        Activity act = getActivity();
        if (act != null) {
            Bundle bundle = new Bundle();
            bundle.putString(ConstantApp.CHAT_RC_ID_KEY, mGroupId);
            bundle.putInt(ConstantApp.CHAT_TYPE_KEY, Enums.RecentContactType.GROUP_TYPE_VALUE);
            NavigationHelper.Companion.getInstance().beginChat(bundle, this);
        }
    }

    private void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final View editView =
                LayoutInflater.from(getContext()).inflate(R.layout.view_edit_group_name, null);
        final EditText edName = editView.findViewById(R.id.ed_name);
        edName.setText(mGroupName);
        edName.setSelection(mGroupName.length());
        builder.setTitle("修改群名");
        builder.setView(editView);
        builder.setPositiveButton("确定", (dialog, which) -> {
            String newName = edName.getText().toString().trim();
            if (!newName.isEmpty() && !newName.equals(mGroupName)) {
                mViewModel.updateGroupName(mGroupId, newName);
            }
            dialog.dismiss();
        });
        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void onBackPressed() {
        back();
//        return true;
    }
}
