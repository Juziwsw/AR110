package com.hiar.ar110.viewmodel;

import android.content.Context;

import com.hiar.ar110.ConstantApp;
import com.hiar.ar110.R;
import com.hiar.ar110.base.BaseViewModel;
import com.hiar.mybaselib.utils.AR110Log;
import com.hiar.mybaselib.utils.liveData.EventMutableLiveData;
import com.hiar.mybaselib.utils.liveData.ReqResult;
import com.hileia.common.enginer.LeiaBoxEngine;
import com.hileia.common.entity.MultVal;
import com.hileia.common.entity.proto.EntityOuterClass;
import com.hileia.common.entity.proto.Enums;
import com.hileia.common.entity.proto.Handler;
import com.hileia.common.manager.ChatManager;
import com.hiscene.imui.bean.CallMsgBody;
import com.hiscene.imui.bean.ImageMsgBody;
import com.hiscene.imui.bean.Message;
import com.hiscene.imui.bean.MsgBody;
import com.hiscene.imui.bean.MsgSendStatus;
import com.hiscene.imui.bean.MsgType;
import com.hiscene.imui.bean.SystemMsgBody;
import com.hiscene.imui.bean.TextMsgBody;
import com.hiscene.imui.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * author: liwf
 * date: 2021/3/17 16:32
 */
public class ChatViewModel extends BaseViewModel {
    private final String TAG = getClass().getSimpleName();
    private String chatId;
    private Context mContext;
    private boolean isGroup = true;
    private EntityOuterClass.Entity.UserInfo mInfo;
    private EventMutableLiveData<Message> messageLiveData = new EventMutableLiveData<>();//新消息
    private EventMutableLiveData<Message> statusLiveData = new EventMutableLiveData<>();//消息状态
    private EventMutableLiveData<ReqResult<List<Message>>> messageListLiveData = new EventMutableLiveData<>();//新消息
    private EventMutableLiveData<EntityOuterClass.Entity.GroupInfo> groupInfoLiveData = new EventMutableLiveData<>();//新消息

    @Override
    public void onNewMessage(int msgId, MultVal multVal) {
        try {
            switch (msgId) {
                case Handler.HandlerMsgIds.HD_MSG_GROUP_LIST_UPDATE_NOTIFY_VALUE:
                    if (isGroup) {
                        EntityOuterClass.Entity.GroupInfo groupInfo = LeiaBoxEngine.getInstance().groupManager().getGroupInfo(chatId);
                        groupInfoLiveData.postValue(groupInfo);
                    }
                    break;
                case Handler.HandlerMsgIds.HD_MSG_CHAT_NEW_MESSAGE_NOTIFY_VALUE:
                    AR110Log.i(TAG, "HD_MSG_CHAT_NEW_MESSAGE_NOTIFY_VALUE");
                    EntityOuterClass.Entity.ChatMsgInfo info = EntityOuterClass.Entity.ChatMsgInfo.parseFrom(multVal.buf);
                    if (info.getId().equals(chatId)) {
                        AR110Log.i(TAG, "info: type: %d , status %d", info.getType(), info.getStatus());
                        Message msg = genMyMessage(info);
                        messageLiveData.postValue(msg);
                    }
                    break;
                case Handler.HandlerMsgIds.HD_MSG_CHAT_MESSAGE_STATUS_CHANGE_VALUE:
                    EntityOuterClass.Entity.ChatMsgInfo status = EntityOuterClass.Entity.ChatMsgInfo.parseFrom(multVal.buf);
                    if (status.getId().equals(chatId)) {
                        AR110Log.i(TAG, "info: type: %d , status %d", status.getType(), status.getStatus());
                        Message msg = genMyMessage(status);
                        statusLiveData.postValue(msg);
                    }
                    break;
                case Handler.HandlerMsgIds.HD_MSG_CHAT_PAGE_MESSAGE_NOTIFY_VALUE:
                    AR110Log.i(TAG, "HD_MSG_CHAT_PAGE_MESSAGE_NOTIFY_VALUE");
                    EntityOuterClass.Entity.ChatMsgData msgList = EntityOuterClass.Entity.ChatMsgData.parseFrom(multVal.buf);
                    if (msgList.getId().equals(chatId)) {
                        List<Message> list = new ArrayList<>();
                        int size = msgList.getMsgsCount();
                        AR110Log.i(TAG, "chat page size: %d", size);
                        for (int i = 0; i < size; i++) {
                            EntityOuterClass.Entity.ChatMsgInfo msgInfo = msgList.getMsgs(i);
                            Message msg = genMyMessage(msgInfo);
                            list.add(msg);
                        }
                        ReqResult<List<Message>> result = new ReqResult<>();
                        result.setStatus(ConstantApp.STATUS_SUCCESS);
                        result.setData(list);
                        messageListLiveData.postValue(result);
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

    public void getMyInfo(Context context) {
        mContext = context;
        mInfo = LeiaBoxEngine.getInstance().accountManager().getUserInfo();
        AR110Log.i(TAG, "myInfo: %s, %s", mInfo.getUserID(), mInfo.getName());
    }

    public String getChatTitle(String chatId, int type) {
        this.chatId = chatId;
        String title = "";
        if (type == Enums.RecentContactType.GROUP_TYPE_VALUE) {
            isGroup = true;
            EntityOuterClass.Entity.GroupInfo groupInfo = LeiaBoxEngine.getInstance().groupManager().getGroupInfo(chatId);
            title = groupInfo.getName();
        } else if (type == Enums.RecentContactType.PEOPLE_TYPE_VALUE) {
            isGroup = false;
            EntityOuterClass.Entity.ContactInfo contactInfo = LeiaBoxEngine.getInstance().contactManager().getContactInfo(chatId);
            title = contactInfo.getName();
        }
        return title;
    }

    public void requestPageUp(int number, long timeStamp) {
        AR110Log.i(TAG, "requestPageUp number: %d timeStamp: %d", number, timeStamp);
        LeiaBoxEngine.getInstance().chatManager().requestPageUp(isGroup, chatId, number, timeStamp);
    }

    public void sendTextMessage(String message) {
        if (!message.isEmpty()) {
            AR110Log.i(TAG, "sendMessage: %s", message);
            LeiaBoxEngine.getInstance().chatManager().sendTextMessage(isGroup, chatId, message);
        }
    }

    public EventMutableLiveData<Message> getMessageLiveData() {
        return messageLiveData;
    }

    public EventMutableLiveData<ReqResult<List<Message>>> getMessageListLiveData() {
        return messageListLiveData;
    }

    public EventMutableLiveData<Message> getStatusLiveData() {
        return statusLiveData;
    }

    public EventMutableLiveData<EntityOuterClass.Entity.GroupInfo> getGroupInfoLiveData() {
        return groupInfoLiveData;
    }

    private Message genMyMessage(EntityOuterClass.Entity.ChatMsgInfo info) {
        MsgType type = MsgType.SYSTEM;
        MsgBody body;
        boolean isSend = info.getUserId().equals(mInfo.getUserID());
        Message msg = new Message(info.getUniqueKey(), type);
        ChatManager.ChatMsgType msgType = ChatManager.ChatMsgType.values()[info.getType()];
        switch (msgType) {
            case CHAT_MSG_TYPE_UNKNOWN:
            case CHAT_MSG_TYPE_EVENT:
            case CHAT_MSG_TYPE_GROUP_INVITE:
            case CHAT_MSG_TYPE_GROUP_KICK:
                body = new SystemMsgBody();
                ((SystemMsgBody) body).setMessage(info.getText());
                body.setLocalMsgType(MsgType.SYSTEM);
                msg.setMsgType(MsgType.SYSTEM);
                break;
            case CHAT_MSG_TYPE_IMAGE:
                body = new ImageMsgBody();
                ((ImageMsgBody) body).setThumbPath(info.getPath());
                if (isSend) {
                    msg.setUserInfo(true, mInfo.getUserID(), mInfo.getName(), mInfo.getHasPhoto(), mInfo.getAvatarUrl());
                } else {
                    EntityOuterClass.Entity.ContactInfo contact = LeiaBoxEngine.getInstance().contactManager().getContactInfo(info.getUserId());
                    msg.setUserInfo(false, contact.getUserID(), contact.getName(), !contact.getAvatarUrl().isEmpty(), contact.getAvatarUrl());
                }
                msg.setMsgType(MsgType.IMAGE);
                AR110Log.i(TAG, "image path: %s", info.getPath());
                break;
            case CHAT_MSG_TYPE_TEXT:
                body = new TextMsgBody();
                ((TextMsgBody) body).setMessage(info.getText());
                if (isSend) {
                    msg.setUserInfo(true, mInfo.getUserID(), mInfo.getName(), mInfo.getHasPhoto(), mInfo.getAvatarUrl());
                } else {
                    EntityOuterClass.Entity.ContactInfo contact = LeiaBoxEngine.getInstance().contactManager().getContactInfo(info.getUserId());
                    msg.setUserInfo(false, contact.getUserID(), contact.getName(), !contact.getAvatarUrl().isEmpty(), contact.getAvatarUrl());
                }
                msg.setMsgType(MsgType.TEXT);
                break;
            case CHAT_MSG_TYPE_CALL:
                body = new CallMsgBody();
                ((CallMsgBody) body).setExtra(info.getCallMembers());
                switch (info.getCallDealType()) {
                    case Enums.CallDealType.UnAnswered_VALUE:
                        if (info.getCallerId().equals(mInfo.getUserID())) {
                            ((CallMsgBody) body).setMessage(mContext.getString(R.string.label_call_other_unanswered));
                        } else {
                            ((CallMsgBody) body).setMessage(mContext.getString(R.string.label_call_unanswered));
                        }
                        break;
                    case Enums.CallDealType.Answered_VALUE:
                        ((CallMsgBody) body).setMessage(String.format(mContext.getString(R.string.label_call_time), TimeUtils.getTimeString((int) info.getCallTime())));
                        break;
                    case Enums.CallDealType.Reject_VALUE:
                        if (info.getCallerId().equals(mInfo.getUserID())) {
                            ((CallMsgBody) body).setMessage(mContext.getString(R.string.label_call_other_rejected));
                        } else {
                            ((CallMsgBody) body).setMessage(mContext.getString(R.string.label_call_rejected));
                        }
                        break;
                    case Enums.CallDealType.Canceled_VALUE:
                        if (info.getCallerId().equals(mInfo.getUserID())) {
                            ((CallMsgBody) body).setMessage(mContext.getString(R.string.label_call_cancelled));
                        } else {
                            ((CallMsgBody) body).setMessage(mContext.getString(R.string.label_call_other_cancelled));
                        }
                        break;
                }
                if (isSend) {
                    msg.setUserInfo(true, mInfo.getUserID(), mInfo.getName(), mInfo.getHasPhoto(), mInfo.getAvatarUrl());
                } else {
                    EntityOuterClass.Entity.ContactInfo contact = LeiaBoxEngine.getInstance().contactManager().getContactInfo(info.getUserId());
                    msg.setUserInfo(false, contact.getUserID(), contact.getName(), !contact.getAvatarUrl().isEmpty(), contact.getAvatarUrl());
                }
                msg.setMsgType(MsgType.CALL_HISTORY);
                break;
            default:
                body = new MsgBody();
                break;
        }
        msg.setBody(body);
        msg.setSentTime(info.getTimestamp());
        msg.setSentStatus(MsgSendStatus.values()[info.getStatus()]);
        return msg;
    }
}
