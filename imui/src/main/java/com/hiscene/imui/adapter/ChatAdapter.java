package com.hiscene.imui.adapter;

import android.content.Context;
import androidx.core.view.ViewCompat;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.util.MultiTypeDelegate;
import com.hiscene.imui.R;
import com.hiscene.imui.bean.AudioMsgBody;
import com.hiscene.imui.bean.CallMsgBody;
import com.hiscene.imui.bean.FileMsgBody;
import com.hiscene.imui.bean.ImageMsgBody;
import com.hiscene.imui.bean.Message;
import com.hiscene.imui.bean.MsgBody;
import com.hiscene.imui.bean.MsgSendStatus;
import com.hiscene.imui.bean.MsgType;
import com.hiscene.imui.bean.SystemMsgBody;
import com.hiscene.imui.bean.TextMsgBody;
import com.hiscene.imui.bean.VideoMsgBody;
import com.hiscene.imui.util.FileUtils;
import com.hiscene.imui.util.GlideUtils;
import com.hiscene.imui.util.TimeUtils;
import com.hiscene.imui.widget.NiceImageView;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

public class ChatAdapter extends BaseQuickAdapter<Message, BaseViewHolder> {

    private static final int TYPE_SEND_TEXT = 1;
    private static final int TYPE_RECEIVE_TEXT = 2;
    private static final int TYPE_SEND_IMAGE = 3;
    private static final int TYPE_RECEIVE_IMAGE = 4;
    private static final int TYPE_SEND_VIDEO = 5;
    private static final int TYPE_RECEIVE_VIDEO = 6;
    private static final int TYPE_SEND_FILE = 7;
    private static final int TYPE_RECEIVE_FILE = 8;
    private static final int TYPE_SEND_AUDIO = 9;
    private static final int TYPE_RECEIVE_AUDIO = 10;
    private static final int TYPE_SEND_CALL = 11;
    private static final int TYPE_RECEIVE_CALL = 12;
    private static final int TYPE_SEND_SYSTEM = 13;
    private static final int TYPE_RECEIVE_SYSTEM = 14;

    private static final int SEND_TEXT = R.layout.item_text_send;
    private static final int RECEIVE_TEXT = R.layout.item_text_receive;
    private static final int SEND_IMAGE = R.layout.item_image_send;
    private static final int RECEIVE_IMAGE = R.layout.item_image_receive;
    private static final int SEND_VIDEO = R.layout.item_video_send;
    private static final int RECEIVE_VIDEO = R.layout.item_video_receive;
    private static final int SEND_FILE = R.layout.item_file_send;
    private static final int RECEIVE_FILE = R.layout.item_file_receive;
    private static final int RECEIVE_AUDIO = R.layout.item_audio_receive;
    private static final int SEND_AUDIO = R.layout.item_audio_send;
    private static final int RECEIVE_CALL = R.layout.item_call_receive;
    private static final int SEND_CALL = R.layout.item_call_send;
    private static final int RECEIVE_SYSTEM = R.layout.item_system_receive;
    private static final int SEND_SYSTEM = R.layout.item_system_send;
    private DecimalFormat decimalFormat = new DecimalFormat(".00");
    private Context mContext;

    public ChatAdapter(List<Message> data, Context context) {
        super(data);
        mContext = context;
        setOnClick(null);
        setMultiTypeDelegate(new MultiTypeDelegate<Message>() {
            @Override
            protected int getItemType(Message entity) {
                boolean isSend = entity.isSend();
                if (MsgType.TEXT == entity.getMsgType()) {
                    return isSend ? TYPE_SEND_TEXT : TYPE_RECEIVE_TEXT;
                } else if (MsgType.IMAGE == entity.getMsgType()) {
                    return isSend ? TYPE_SEND_IMAGE : TYPE_RECEIVE_IMAGE;
                } else if (MsgType.VIDEO == entity.getMsgType()) {
                    return isSend ? TYPE_SEND_VIDEO : TYPE_RECEIVE_VIDEO;
                } else if (MsgType.FILE == entity.getMsgType()) {
                    return isSend ? TYPE_SEND_FILE : TYPE_RECEIVE_FILE;
                } else if (MsgType.AUDIO == entity.getMsgType()) {
                    return isSend ? TYPE_SEND_AUDIO : TYPE_RECEIVE_AUDIO;
                } else if (MsgType.CALL_HISTORY == entity.getMsgType()) {
                    return isSend ? TYPE_SEND_CALL : TYPE_RECEIVE_CALL;
                } else if (MsgType.SYSTEM == entity.getMsgType()) {
                    return isSend ? TYPE_SEND_SYSTEM : TYPE_RECEIVE_SYSTEM;
                }
                return 0;
            }
        });
        getMultiTypeDelegate().registerItemType(TYPE_SEND_TEXT, SEND_TEXT)
                .registerItemType(TYPE_RECEIVE_TEXT, RECEIVE_TEXT)
                .registerItemType(TYPE_SEND_IMAGE, SEND_IMAGE)
                .registerItemType(TYPE_RECEIVE_IMAGE, RECEIVE_IMAGE)
                .registerItemType(TYPE_SEND_VIDEO, SEND_VIDEO)
                .registerItemType(TYPE_RECEIVE_VIDEO, RECEIVE_VIDEO)
                .registerItemType(TYPE_SEND_FILE, SEND_FILE)
                .registerItemType(TYPE_RECEIVE_FILE, RECEIVE_FILE)
                .registerItemType(TYPE_SEND_AUDIO, SEND_AUDIO)
                .registerItemType(TYPE_RECEIVE_AUDIO, RECEIVE_AUDIO)
                .registerItemType(TYPE_SEND_CALL, SEND_CALL)
                .registerItemType(TYPE_RECEIVE_CALL, RECEIVE_CALL)
                .registerItemType(TYPE_SEND_SYSTEM, SEND_SYSTEM)
                .registerItemType(TYPE_RECEIVE_SYSTEM, RECEIVE_SYSTEM);
    }

    @Override
    protected void convert(BaseViewHolder helper, Message item) {
        setContent(helper, item);
        setStatus(helper, item);
    }

    private void setStatus(BaseViewHolder helper, Message item) {
        MsgBody msgContent = item.getBody();
        if (msgContent instanceof TextMsgBody || msgContent instanceof SystemMsgBody
                || msgContent instanceof FileMsgBody) {
            //只需要设置自己发送的状态
            MsgSendStatus sentStatus = item.getSentStatus();
            if (item.isSend()) {
                switch (sentStatus) {
                    case CHAT_MSG_STATUS_SENDING:
                        helper.setVisible(R.id.chat_item_progress, true).setVisible(R.id.chat_item_fail, false);
                        break;
                    case CHAT_MSG_STATUS_SEND_FAILED:
                        helper.setVisible(R.id.chat_item_progress, false).setVisible(R.id.chat_item_fail, true);
                        break;
                    case CHAT_MSG_STATUS_SENT:
                        helper.setVisible(R.id.chat_item_progress, false).setVisible(R.id.chat_item_fail, false);
                        break;
                }
            }
        } else if (msgContent instanceof CallMsgBody) {
            //只需要设置自己发送的状态
            MsgSendStatus sentStatus = item.getSentStatus();
            if (item.isSend()) {
                switch (sentStatus) {
                    case CHAT_MSG_STATUS_SENDING:
                        helper.setVisible(R.id.chat_item_progress, true).setVisible(R.id.chat_item_fail, false);
                        break;
                    case CHAT_MSG_STATUS_SEND_FAILED:
                        helper.setVisible(R.id.chat_item_progress, false).setVisible(R.id.chat_item_fail, true);
                        break;
                    case CHAT_MSG_STATUS_SENT:
                        helper.setVisible(R.id.chat_item_progress, false).setVisible(R.id.chat_item_fail, false);
                        break;
                }
            }
        } else if (msgContent instanceof ImageMsgBody) {
            if (item.isSend()) {
                MsgSendStatus sentStatus = item.getSentStatus();
                switch (sentStatus) {
                    case CHAT_MSG_STATUS_SENDING:
                        helper.setVisible(R.id.chat_item_progress, false).setVisible(R.id.chat_item_fail, false);
                        break;
                    case CHAT_MSG_STATUS_SEND_FAILED:
                        helper.setVisible(R.id.chat_item_progress, false).setVisible(R.id.chat_item_fail, true);
                        break;
                    case CHAT_MSG_STATUS_SENT:
                        helper.setVisible(R.id.chat_item_progress, false).setVisible(R.id.chat_item_fail, false);
                        break;
                }
            }
        }
    }

    public void resetLastShowTimeState() {
        mLastShowTime = TimeUtils.getCurTimeMills();
        mLastShowTimeMsgId = 0;
    }

    private long mLastShowTime = TimeUtils.getCurTimeMills();
    private long mLastShowTimeMsgId = 0;

    //是否显示消息时间
    private boolean isShowTime(BaseViewHolder helper, Message msg) {
        if (Math.abs(msg.getSentTime() - mLastShowTime) > 5 * 60 * 1000 ||
                (getItemCount() > 0 && helper.getAdapterPosition() == getItemCount() - 1) ||
                msg.getUniqueKey() == mLastShowTimeMsgId) {
            mLastShowTime = msg.getSentTime();
            mLastShowTimeMsgId = msg.getUniqueKey();
            return true;
        }
        return false;
    }

    private void setContent(BaseViewHolder helper, Message item) {
        if (isShowTime(helper, item)) {
            helper.setVisible(R.id.item_tv_time, true);
            helper.setText(R.id.item_tv_time, TimeUtils.getTimeStringAutoShort(mContext, new Date(item.getSentTime()), true));
        } else {
            helper.setVisible(R.id.item_tv_time, false);
        }

        if (item.getMsgType().equals(MsgType.SYSTEM)) {
            SystemMsgBody msgBody = (SystemMsgBody) item.getBody();
            helper.setText(R.id.item_tv_system, msgBody.getMessage());
        } else {
            ViewCompat.setTransitionName(helper.getView(R.id.chat_item_header), item.getAvatarUrl());
            if (item.isHasPhoto()) {
                GlideUtils.loadAvatarNoCache(mContext, helper.getView(R.id.chat_item_header), item.getAvatarUrl(), R.drawable.default_portrait);
            } else {
                ((NiceImageView) helper.getView(R.id.chat_item_header)).setTextSeed(item.getUserName());
            }
            switch (item.getMsgType()) {
                case TEXT: {
                    TextMsgBody msgBody = (TextMsgBody) item.getBody();
                    helper.setText(R.id.chat_item_content_text, msgBody.getMessage());
                    if (!item.isSend()) {
                        helper.setText(R.id.chat_item_name, item.getUserName());
                    }
                    break;
                }
                case IMAGE: {
                    ImageMsgBody msgBody = (ImageMsgBody) item.getBody();
                    if (TextUtils.isEmpty(msgBody.getThumbPath())) {
                        GlideUtils.loadChatImage(mContext, msgBody.getThumbUrl(), helper.getView(R.id.bivPic));
                    } else {
                        File file = new File(msgBody.getThumbPath());
                        if (file.exists()) {
                            GlideUtils.loadChatImage(mContext, msgBody.getThumbPath(), helper.getView(R.id.bivPic));
                        } else {
                            GlideUtils.loadChatImage(mContext, msgBody.getThumbUrl(), helper.getView(R.id.bivPic));
                        }
                    }
                    break;
                }
                case VIDEO: {
                    VideoMsgBody msgBody = (VideoMsgBody) item.getBody();
                    File file = new File(msgBody.getExtra());
                    if (file.exists()) {
                        GlideUtils.loadChatImage(mContext, msgBody.getExtra(), helper.getView(R.id.bivPic));
                    } else {
                        GlideUtils.loadChatImage(mContext, msgBody.getExtra(), helper.getView(R.id.bivPic));
                    }
                    break;
                }
                case FILE: {
                    FileMsgBody msgBody = (FileMsgBody) item.getBody();
                    helper.setText(R.id.msg_tv_file_name, msgBody.getDisplayName());
                    helper.setText(R.id.msg_tv_file_size, FileUtils.formatFileSize(msgBody.getSize()));
                    break;
                }
                case AUDIO: {
                    AudioMsgBody msgBody = (AudioMsgBody) item.getBody();
                    helper.setText(R.id.tvDuration, msgBody.getDuration() + "\"");
                    break;
                }
                case CALL_HISTORY:
                    CallMsgBody callMsgBody = (CallMsgBody) item.getBody();
                    helper.setText(R.id.tvHint, callMsgBody.getMessage());
                    if (!item.isSend()) {
                        helper.setText(R.id.chat_item_name, item.getUserName());
                    }
                    break;
            }
        }
    }

    private void setOnClick(Message item) {
//        addChildClickViewIds(R.id.chat_item_layout_content);
//        addChildClickViewIds(R.id.chat_item_header);
    }

    public synchronized void updateMsg(Message msg) {
        for (Message m : getData()) {
            if (m.getUniqueKey() == msg.getUniqueKey()) {
                int index = getData().indexOf(m);
                getData().remove(index);
                getData().add(index, msg);
                notifyItemChanged(index);
                break;
            }
        }
    }

    public void addToStart(Message msg) {
        addData(0, msg);
    }

}
