package com.hiscene.imui.bean;


public class Message {

    private long uniqueKey;
    private MsgType msgType;
    private MsgBody body;
    private MsgSendStatus sentStatus;
    private boolean isSend = false;
    private long sentTime;
    private String userId;
    private String userName;
    private String avatarUrl;
    private boolean hasPhoto;
    private String callMember;

    public Message(long key, MsgType type) {
        this.uniqueKey = key;
        this.msgType = type;
    }

    public void setUserInfo(boolean isSend, String userId, String userName, boolean hasPhoto, String avatarUrl) {
        this.isSend = isSend;
        this.userId = userId;
        this.userName = userName;
        this.avatarUrl = avatarUrl;
        this.hasPhoto = hasPhoto;
    }

    public String getCallMember() {
        return callMember;
    }

    public void setCallMember(String callMember) {
        this.callMember = callMember;
    }

    public long getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(long uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isHasPhoto() {
        return hasPhoto;
    }

    public void setHasPhoto(boolean hasPhoto) {
        this.hasPhoto = hasPhoto;
    }

    public MsgType getMsgType() {
        return msgType;
    }

    public void setMsgType(MsgType msgType) {
        this.msgType = msgType;
    }

    public MsgBody getBody() {
        return body;
    }

    public void setBody(MsgBody body) {
        this.body = body;
    }

    public boolean isSend() {
        return isSend;
    }

    public void setSend(boolean send) {
        isSend = send;
    }

    public MsgSendStatus getSentStatus() {
        return sentStatus;
    }

    public void setSentStatus(MsgSendStatus sentStatus) {
        this.sentStatus = sentStatus;
    }

    public long getSentTime() {
        return sentTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setSentTime(long sentTime) {
        this.sentTime = sentTime;
    }
}
