package com.hiar.ar110.fragment;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.hiar.ar110.ConstantApp;
import com.hiar.ar110.R;
import com.hiar.ar110.adapter.ContactSelectAdapter;
import com.hiar.ar110.adapter.ContactSelectedAdapter;
import com.hiar.ar110.base.BaseFragment;
import com.hiar.ar110.helper.NavigationHelper;
import com.hiar.ar110.viewmodel.ContactViewModel;
import com.hiar.ar110.viewmodel.GroupViewModel;
import com.hiar.mybaselib.utils.toast.ToastUtils;
import com.hileia.common.entity.proto.EntityOuterClass;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * author: liwf
 * date: 2021/3/20 10:58
 * 添加群成员界面
 */
public class GroupManagerFragment extends BaseFragment {
    private ImageView imgBack;
    private TextView tvTitle, tvDetail, tvConfirm;
    private RecyclerView mRVCreateGroup, mRVSelectedContacts;
    private GroupViewModel mGroupViewModel;
    private ContactViewModel mContactViewModel;
    private String mGroupId;
    private ContactSelectAdapter mContactAdapter;
    private ContactSelectedAdapter mContactSelectedAdapter;
    private LinearLayoutManager verticalLayoutManager;
    private List<EntityOuterClass.Entity.ContactInfo> mContacts = new ArrayList<>();//所有联系人
    private List<EntityOuterClass.Entity.ContactInfo> selectedContacts = new ArrayList<>();//被勾选的联系人
    private List<EntityOuterClass.Entity.ContactInfo> filterContacts = new ArrayList<>();//被搜索出的联系人

    @Override
    public int getLayoutId() {
        return R.layout.fragment_add_member;
    }

    @Override
    public void initData() {
        mGroupId = getArguments().getString(ConstantApp.CHAT_GROUP_ID_KEY);
        mGroupViewModel = getViewModel(GroupViewModel.class);
        mContactViewModel = getViewModel(ContactViewModel.class);
        mGroupViewModel.setGroupId(mGroupId);
    }

    @Override
    public void initView(View view) {
        imgBack = view.findViewById(R.id.img_back);
        tvTitle = view.findViewById(R.id.tv_title);
        tvDetail = view.findViewById(R.id.tv_detail);
        mRVCreateGroup = view.findViewById(R.id.create_group_rvContacts);
        mRVSelectedContacts = view.findViewById(R.id.rvSelectedContacts);
        tvConfirm = view.findViewById(R.id.create_group_confirm_tv);

        verticalLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRVCreateGroup.setLayoutManager(verticalLayoutManager);
        mContactAdapter = new ContactSelectAdapter(getContext());
        mContactAdapter.selectedContact = selectedContacts;
        mRVCreateGroup.setAdapter(mContactAdapter);

        mContactSelectedAdapter = new ContactSelectedAdapter(getContext());
        mContactSelectedAdapter.contactList = selectedContacts;
        mRVSelectedContacts.setAdapter(mContactSelectedAdapter);
    }

    @Override
    public void initListener() {
        imgBack.setOnClickListener(v -> {
            back();
        });
        mContactAdapter.setOnItemCheckChangeListener((checkBox, contact, isChecked) -> GroupManagerFragment.this.onCheckedChanged(checkBox, contact, isChecked));

        mContactViewModel.getMutableUserStatus().observe(this, listReqResult -> {
            if (listReqResult.getStatus() == ConstantApp.STATUS_SUCCESS) {
                for (EntityOuterClass.Entity.ContactInfo contactInfo : listReqResult.getData()) {
                    updateContactsInfo(contactInfo);
                }
                refreshView();
            }
        });
        tvConfirm.setOnClickListener(v -> {
            if (selectedContacts.size() == 0) {

            } else {
                mGroupViewModel.addGroupMember(mGroupId, selectedContacts);
            }
        });
        mGroupViewModel.getInviteLiveData().observe(this, groupInfoReqResult -> {
            if (groupInfoReqResult.getStatus() == ConstantApp.STATUS_SUCCESS) {
                ToastUtils.show("添加成功");
            } else {
                ToastUtils.show("添加失败");
            }
            back();
        });
    }

    private void back() {
        NavigationHelper.Companion.getInstance().beginGroupDetail(mGroupId, this);
    }

    @Override
    public void requestData() {
        List<String> groupMemberIds = mGroupViewModel.getGroupMemberIds(mGroupId);
        mContacts = mContactViewModel.getContactList(groupMemberIds);
        refreshView();
        selectedNotifyDataSetChanged();
    }

    private void refreshView() {
        if (mContacts.size() > 0) {
            mContactAdapter.contactList = mContacts;
            notifyDataSetChanged();
        }
    }

    private void notifyDataSetChanged() {
        mContactAdapter.notifyDataSetChanged();
    }

    private void selectedNotifyDataSetChanged() {
        String selectedCountConfirm = getResources().getString(R.string.selected_count_confirm);
        String text = String.format(selectedCountConfirm, selectedContacts.size()+"");
        tvConfirm.setText(text);
        mContactSelectedAdapter.notifyDataSetChanged();
    }

    private void updateContactsInfo(EntityOuterClass.Entity.ContactInfo contactInfo) {
        for (EntityOuterClass.Entity.ContactInfo member : mContacts) {
            if (member.getUserID().equals(contactInfo.getUserID())) {
                int index = mContacts.indexOf(member);
                mContacts.remove(member);
                mContacts.add(index, contactInfo);
                break;
            }
        }
    }

    private void onCheckedChanged(CompoundButton checkBox, EntityOuterClass.Entity.ContactInfo contact, Boolean isChecked) {
        if (isChecked && !contain(contact)) {
            selectedContacts.add(contact);
            mContactSelectedAdapter.contactList = selectedContacts;
            selectedNotifyDataSetChanged();
        } else if (!isChecked && contain(contact)) {
            remove(contact);
            mContactSelectedAdapter.contactList = selectedContacts;
            selectedNotifyDataSetChanged();
        }
    }

    private boolean contain(EntityOuterClass.Entity.ContactInfo contact) {
        for (EntityOuterClass.Entity.ContactInfo select : selectedContacts) {
            if (select.getUserID().equals(contact.getUserID())) {
                return true;
            }
        }
        return false;
    }

    private void remove(EntityOuterClass.Entity.ContactInfo contact) {
        Iterator<EntityOuterClass.Entity.ContactInfo> it = selectedContacts.iterator();
        while (it.hasNext()) {
            EntityOuterClass.Entity.ContactInfo s = it.next();
            if (s.getUserID().equals(contact.getUserID())) {
                it.remove();
            }
        }
    }

    @Override
    public void onBackPressed() {
        back();
    }
}
