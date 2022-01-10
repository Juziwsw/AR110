package com.hiar.ar110.viewmodel;

import com.hiar.ar110.base.BaseViewModel;
import com.hiar.mybaselib.utils.liveData.EventMutableLiveData;
import com.hileia.common.enginer.LeiaBoxEngine;
import com.hileia.common.entity.MultVal;
import com.hileia.common.entity.proto.EntityOuterClass;
import com.hileia.common.entity.proto.Handler;
import com.hileia.common.manager.ChatManager;
import com.hiar.mybaselib.utils.AR110Log;

/**
 * author: liwf
 * date: 2021/3/24 9:27
 */
public class RecentContactsViewModel extends BaseViewModel {
    EventMutableLiveData<Boolean> newMsgLiveData = new EventMutableLiveData();
    EventMutableLiveData<EntityOuterClass.Entity.RecentContactList> recentContactLiveData = new EventMutableLiveData();

    @Override
    public void onNewMessage(int msgId, MultVal multVal) {
        try {
            switch (msgId) {
                case Handler.HandlerMsgIds.HD_MSG_CHAT_NEW_MESSAGE_NOTIFY_VALUE:
                    AR110Log.i(getTAG(), "HD_MSG_CHAT_NEW_MESSAGE_NOTIFY");
                    EntityOuterClass.Entity.ChatMsgInfo chatMsgInfo = EntityOuterClass.Entity.ChatMsgInfo.parseFrom(multVal.buf);
                    EntityOuterClass.Entity.UserInfo userInfo = LeiaBoxEngine.getInstance().accountManager().getUserInfo();
                    if (chatMsgInfo.getType() == ChatManager.ChatMsgType.CHAT_MSG_TYPE_TEXT.ordinal()) {
                        if (!chatMsgInfo.getUserId().equals(userInfo.getUserID())) {
//                            SoundPlayUtil.getInstance(Utils.getContext()).playReceiveMsgSound();
                        }
                    }
                    newMsgLiveData.postValue(true);
                    break;
                case Handler.HandlerMsgIds.HD_MSG_RECENTCONTACTS_UPDATE_NOTIFY_VALUE:
                    AR110Log.i(getTAG(), "HD_MSG_RECENTCONTACTS_UPDATE_NOTIFY");
                    EntityOuterClass.Entity.RecentContactList recentContactList = EntityOuterClass.Entity.RecentContactList.parseFrom(multVal.buf);
                    recentContactLiveData.postValue(recentContactList);
                    break;
                case Handler.HandlerMsgIds.HD_MSG_GROUP_LIST_UPDATE_NOTIFY_VALUE:
                    AR110Log.i(getTAG(), "HD_MSG_GROUP_LIST_UPDATE_NOTIFY");
                    newMsgLiveData.postValue(true);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            AR110Log.i(getTAG(), e.toString());
            e.printStackTrace();
        }
    }

    public EventMutableLiveData<EntityOuterClass.Entity.RecentContactList> getRecentContact() {
        return recentContactLiveData;
    }

    public EventMutableLiveData<Boolean> getNewMsg() {
        return newMsgLiveData;
    }

    public EntityOuterClass.Entity.RecentContactList getRecentContacts() {
        return LeiaBoxEngine.getInstance().recentContactsManager().getRecentContacts();
    }
}
