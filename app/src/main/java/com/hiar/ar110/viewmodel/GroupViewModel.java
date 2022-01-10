package com.hiar.ar110.viewmodel;

import com.hiar.ar110.ConstantApp;
import com.hiar.ar110.base.BaseViewModel;
import com.hiar.mybaselib.utils.liveData.EventMutableLiveData;
import com.hiar.mybaselib.utils.liveData.ReqResult;
import com.hileia.common.enginer.LeiaBoxEngine;
import com.hileia.common.entity.MultVal;
import com.hileia.common.entity.proto.EntityOuterClass;
import com.hileia.common.entity.proto.Handler;
import com.hiar.mybaselib.utils.AR110Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * author: liwf
 * date: 2021/3/19 11:38
 */
public class GroupViewModel extends BaseViewModel {
    private final String TAG = getClass().getSimpleName();
    private String groupId;
    private EventMutableLiveData<ReqResult<EntityOuterClass.Entity.GroupInfo>> groupUpdateLiveData = new EventMutableLiveData<>();//新消息
    private EventMutableLiveData<ReqResult<EntityOuterClass.Entity.GroupInfo>> inviteLiveData = new EventMutableLiveData<>();//新消息
    private EventMutableLiveData<String> lastUpdateLiveData = new EventMutableLiveData<>();//新消息

    @Override
    public void onNewMessage(int msgId, MultVal multVal) {
        try {
            switch (msgId) {
                case Handler.HandlerMsgIds.HD_MSG_GROUP_INVITE_SUCCESS_VALUE:
                    EntityOuterClass.Entity.GroupInfo groupInfo = EntityOuterClass.Entity.GroupInfo.parseFrom(multVal.buf);
                    AR110Log.i(TAG, "HD_MSG_GROUP_INVITE_SUCCESS %s %s", groupInfo.getGroupId(), groupInfo.getName());
                    if (groupId.equals(groupInfo.getGroupId())) {
                        ReqResult<EntityOuterClass.Entity.GroupInfo> result = new ReqResult<>();
                        result.setStatus(ConstantApp.STATUS_SUCCESS);
                        result.setData(groupInfo);
                        inviteLiveData.postValue(result);
                    }
                    AR110Log.i(TAG, "HD_MSG_GROUP_INVITE_SUCCESS");
                    break;
                case Handler.HandlerMsgIds.HD_MSG_GROUP_INVITE_FAILDED_VALUE:
                    ReqResult<EntityOuterClass.Entity.GroupInfo> res = new ReqResult<>();
                    res.setStatus(ConstantApp.STATUS_FAIL);
                    inviteLiveData.postValue(res);
                    AR110Log.i(TAG, "HD_MSG_GROUP_INVITE_FAILDED");
                    break;
                case Handler.HandlerMsgIds.HD_MSG_GROUP_UPDATE_SUCCESS_VALUE:
                    EntityOuterClass.Entity.GroupInfo gInfo = EntityOuterClass.Entity.GroupInfo.parseFrom(multVal.buf);
                    AR110Log.i(TAG, "HD_MSG_GROUP_UPDATE_SUCCESS %s %s", gInfo.getGroupId(), gInfo.getName());
                    if (groupId.equals(gInfo.getGroupId())) {
                        ReqResult<EntityOuterClass.Entity.GroupInfo> updateResult = new ReqResult<>();
                        updateResult.setStatus(ConstantApp.STATUS_SUCCESS);
                        updateResult.setData(gInfo);
                        groupUpdateLiveData.postValue(updateResult);
                    }
                    break;
                case Handler.HandlerMsgIds.HD_MSG_GROUP_UPDATE_FAILDED_VALUE:
                    AR110Log.i(TAG, "HD_MSG_GROUP_UPDATE_FAILDED");
                    ReqResult<EntityOuterClass.Entity.GroupInfo> updateRes = new ReqResult<>();
                    updateRes.setStatus(ConstantApp.STATUS_SUCCESS);
                    groupUpdateLiveData.postValue(updateRes);
                    break;
                case Handler.HandlerMsgIds.HD_MSG_GROUP_LIST_UPDATE_NOTIFY_VALUE:
                    AR110Log.i(TAG, "HD_MSG_GROUP_LIST_UPDATE_NOTIFY");
                    lastUpdateLiveData.postValue("HD_MSG_GROUP_LIST_UPDATE_NOTIFY_VALUE");
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            AR110Log.i(TAG, e.toString());
            e.printStackTrace();
        }
    }

    public EventMutableLiveData<ReqResult<EntityOuterClass.Entity.GroupInfo>> getGroupUpdateLiveData() {
        return groupUpdateLiveData;
    }

    public EventMutableLiveData<ReqResult<EntityOuterClass.Entity.GroupInfo>> getInviteLiveData() {
        return inviteLiveData;
    }

    public EventMutableLiveData<String> getLastUpdateLiveData() {
        return lastUpdateLiveData;
    }

    public void setGroupId(String gId) {
        groupId = gId;
    }

    public List<String> getGroupMemberIds(String groupId) {
        AR110Log.i(TAG, "getGroupMemberIds groupId: %s", groupId);
        List<String> list = new ArrayList<>();
        EntityOuterClass.Entity.GroupInfo groupInfo = LeiaBoxEngine.getInstance().groupManager().getGroupInfo(groupId);
        AR110Log.i(TAG, "group id: %s, group name: %s, memberCount: %d", groupInfo.getGroupId(), groupInfo.getName(), groupInfo.getMemberCount());
        for (EntityOuterClass.Entity.GroupMemberInfo member : groupInfo.getMemberList()) {
            AR110Log.i(TAG, "member : %s id: %s", member.getName(), member.getUserID());
            list.add(member.getUserID());
        }
        return list;
    }

    public List<EntityOuterClass.Entity.ContactInfo> getGroupMembers(String groupId) {
        AR110Log.i(TAG, "getGroupDetail groupId: %s", groupId);
        List<EntityOuterClass.Entity.ContactInfo> list = new ArrayList<>();
        EntityOuterClass.Entity.GroupInfo groupInfo = LeiaBoxEngine.getInstance().groupManager().getGroupInfo(groupId);
        AR110Log.i(TAG, "group id: %s, group name: %s, memberCount: %d", groupInfo.getGroupId(), groupInfo.getName(), groupInfo.getMemberCount());
        EntityOuterClass.Entity.UserInfo mInfo = LeiaBoxEngine.getInstance().accountManager().getUserInfo();
        EntityOuterClass.Entity.ContactInfo.Builder contactInfoBuilder = EntityOuterClass.Entity.ContactInfo.newBuilder();
        contactInfoBuilder.setName(mInfo.getName());
        contactInfoBuilder.setHasPhoto(mInfo.getHasPhoto());
        contactInfoBuilder.setAvatarUrl(mInfo.getAvatarUrl());
        contactInfoBuilder.setUserID(mInfo.getUserID());
        list.add(contactInfoBuilder.build());
        for (EntityOuterClass.Entity.GroupMemberInfo member : groupInfo.getMemberList()) {
            AR110Log.i(TAG, "member : %s id: %s", member.getName(), member.getUserID());
            if (!member.getUserID().equals(mInfo.getUserID())) {
                EntityOuterClass.Entity.ContactInfo info = LeiaBoxEngine.getInstance().contactManager().getContactInfo(member.getUserID());
                AR110Log.i(TAG, "contact info name: %s, id: %s", info.getName(), info.getUserID());
                list.add(info);
            }
        }
        return list;
    }

    public EntityOuterClass.Entity.GroupInfo getGroupInfo(String groupId) {
        EntityOuterClass.Entity.GroupInfo groupInfo = LeiaBoxEngine.getInstance().groupManager().getGroupInfo(groupId);
        AR110Log.i(TAG, "getGroupInfo groupId-%s : name-%s, id-%s, count-%d", groupId, groupInfo.getName(), groupInfo.getGroupId(), groupInfo.getMemberCount());
        return groupInfo;
    }

    public void addGroupMember(String groupId, List<EntityOuterClass.Entity.ContactInfo> userList) {
        String[] users = new String[userList.size()];
        for (int i = 0; i < userList.size(); i++) {
            EntityOuterClass.Entity.ContactInfo info = userList.get(i);
            AR110Log.i(TAG, "addGroupMember name %s id %s", info.getName(), info.getUserID());
            users[i] = info.getUserID();
        }
        AR110Log.i(TAG, "inviteUserJoinGroup groupId: %s users: %s", groupId, Arrays.toString(users));
        LeiaBoxEngine.getInstance().groupManager().inviteUserJoinGroup(groupId, users);
    }

    public void updateGroupName(String groupId, String newName) {
        LeiaBoxEngine.getInstance().groupManager().updateGroup(groupId, newName);
    }
}
