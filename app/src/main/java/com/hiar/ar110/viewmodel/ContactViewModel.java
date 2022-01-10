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
import java.util.List;

/**
 * author: liwf
 * date: 2021/3/23 11:34
 */
public class ContactViewModel extends BaseViewModel {
    private final String TAG = getClass().getSimpleName();
    private EventMutableLiveData<ReqResult<List<EntityOuterClass.Entity.ContactInfo>>> mutableUserStatus = new EventMutableLiveData();

    @Override
    public void onNewMessage(int msgId, MultVal multVal) {
        try {
            switch (msgId) {
                case Handler.HandlerMsgIds.HD_MSG_CONTACT_GETSTATUS_SUCCESS_VALUE:
                case Handler.HandlerMsgIds.HD_MSG_CONTACT_LASTSTATUSNOTIFY_SUCCESS_VALUE:
                    EntityOuterClass.Entity.UserStatusData userStatusData = EntityOuterClass.Entity.UserStatusData.parseFrom(multVal.buf);
                    List<EntityOuterClass.Entity.ContactInfo> contacts = new ArrayList<>();
                    for (EntityOuterClass.Entity.UserStatus status : userStatusData.getUserStatuArrayList()) {
                        EntityOuterClass.Entity.ContactInfo contact = LeiaBoxEngine.getInstance().contactManager().getContactInfo(status.getUserID());
                        if (!contact.getUserID().isEmpty()) {
                            contacts.add(contact);
                        }
                    }
                    if (contacts.size() > 0) {
                        ReqResult<List<EntityOuterClass.Entity.ContactInfo>> contactResult = new ReqResult<>();
                        contactResult.setStatus(ConstantApp.STATUS_SUCCESS);
                        contactResult.setData(contacts);
                        mutableUserStatus.postValue(contactResult);
                    }
                    break;
                case Handler.HandlerMsgIds.HD_MSG_CONTACT_INFO_UPDATE_NOTIFY_VALUE:
                    AR110Log.i(TAG, "HD_MSG_CONTACT_INFO_UPDATE_NOTIFY");
                    List<EntityOuterClass.Entity.ContactInfo> contactList = new ArrayList<>();
                    EntityOuterClass.Entity.ContactInfo contact = EntityOuterClass.Entity.ContactInfo.parseFrom(multVal.buf);
                    if (!contact.getUserID().isEmpty()) {
                        contactList.add(contact);
                    }
                    if (contactList.size() > 0) {
                        ReqResult<List<EntityOuterClass.Entity.ContactInfo>> contactResult = new ReqResult<>();
                        contactResult.setStatus(ConstantApp.STATUS_SUCCESS);
                        contactResult.setData(contactList);
                        mutableUserStatus.postValue(contactResult);
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            AR110Log.i(TAG, e.toString());
            e.printStackTrace();
        }
    }

    public EventMutableLiveData<ReqResult<List<EntityOuterClass.Entity.ContactInfo>>> getMutableUserStatus() {
        return mutableUserStatus;
    }

    public List<EntityOuterClass.Entity.ContactInfo> getContactList() {
        EntityOuterClass.Entity.ContactsData contactsData = LeiaBoxEngine.getInstance().contactManager().getCorpContactsData(true);
        int size = contactsData.getContactsArrayCount();
        List<EntityOuterClass.Entity.ContactInfo> contacts = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            contacts.add(contactsData.getContactsArray(i));
        }
        return contacts;
    }

    public List<EntityOuterClass.Entity.ContactInfo> getContactList(List<String> filterList) {
        EntityOuterClass.Entity.ContactsData contactsData = LeiaBoxEngine.getInstance().contactManager().getCorpContactsData(true);
        int size = contactsData.getContactsArrayCount();
        List<EntityOuterClass.Entity.ContactInfo> contacts = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            EntityOuterClass.Entity.ContactInfo info = contactsData.getContactsArray(i);
            if (!filterList.contains(info.getUserID())) {
                contacts.add(contactsData.getContactsArray(i));
            }
        }
        return contacts;
    }
}
