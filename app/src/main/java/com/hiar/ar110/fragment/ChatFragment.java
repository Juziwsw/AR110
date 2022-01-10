package com.hiar.ar110.fragment;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hiar.ar110.ConstantApp;
import com.hiar.ar110.R;
import com.hiar.ar110.base.BaseFragment;
import com.hiar.ar110.helper.NavigationHelper;
import com.hiar.ar110.viewmodel.ChatViewModel;
import com.hiar.mybaselib.utils.AR110Log;
import com.hiar.mybaselib.utils.EmoticonsKeyboardUtils;
import com.hileia.common.entity.proto.Enums;
import com.hiscene.imui.adapter.ChatAdapter;
import com.hiscene.imui.util.ChatUiHelper;
import com.hiscene.imui.util.StringUtils;
import com.hiscene.imui.widget.StateButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * author: liwf
 * date: 2021/3/17 15:12
 * 聊天界面
 */
public class ChatFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    private ImageView imgBack, imgType;
    private TextView tvTitle, tvDetail, tvCall;
    private ChatViewModel mViewModel;
    private int currentChatType;
    private String recentId;//最近消息 id
    private ChatAdapter mChatAdapter;
    private RecyclerView mChatListView;
    private SwipeRefreshLayout mSwipeChat;
    private RelativeLayout mContentLayout, mBottomLayout;
    private AppCompatEditText mEdMsg;
    private StateButton mSendBtn;
    private long lastMsgTimeStamp = 0;
    private int totalMessage = 0;//当前获取到的消息总数量
    private final int REQUEST_MESSAGE_COUNT = 20;//单次请求的消息数量
    private final int CHAT_TYPE_VIDEO = 1;
    private final int CHAT_TYPE_TEXT = 2;
    private int currentMode = CHAT_TYPE_TEXT;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_chat;
    }

    @Override
    public void initData() {
        AR110Log.i(getTAG(), "initData");
        totalMessage = 0;
        lastMsgTimeStamp = 0;
        currentChatType = getArguments().getInt(ConstantApp.CHAT_TYPE_KEY, 0);
        recentId = getArguments().getString(ConstantApp.CHAT_RC_ID_KEY);
        mViewModel = getViewModel(ChatViewModel.class);
    }

    @Override
    public void initView(View view) {
        AR110Log.i(getTAG(), "initView");
        imgBack = view.findViewById(R.id.img_back);
        tvTitle = view.findViewById(R.id.tv_title);
        tvDetail = view.findViewById(R.id.tv_detail);
        imgType = view.findViewById(R.id.img_chat_type);
        tvCall = view.findViewById(R.id.tv_call);
        mChatListView = view.findViewById(R.id.rv_chat_list);
        mSwipeChat = view.findViewById(R.id.swipe_chat);
        mContentLayout = view.findViewById(R.id.llContent);
        mBottomLayout = view.findViewById(R.id.bottom_layout);
        mEdMsg = view.findViewById(R.id.et_content);
        mSendBtn = view.findViewById(R.id.btn_send);
        mChatAdapter = new ChatAdapter(new ArrayList<>(), getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mChatListView.setLayoutManager(linearLayoutManager);
        mChatListView.setAdapter(mChatAdapter);
        updateModeUI();
    }

    @Override
    public void initListener() {
        AR110Log.i(getTAG(), "initListener");
        imgBack.setOnClickListener(v -> {
            back();
        });
        tvDetail.setOnClickListener(v -> {
            Activity act = getActivity();
            if (act != null) {
                NavigationHelper.Companion.getInstance().beginGroupDetail(recentId, this);
            }
        });
//        imgType.setOnClickListener(v -> {
//            if (currentMode == CHAT_TYPE_TEXT) {
//                currentMode = CHAT_TYPE_VIDEO;
//            } else if (currentMode == CHAT_TYPE_VIDEO) {
//                currentMode = CHAT_TYPE_TEXT;
//            }
//            updateModeUI();
//        });
        mSwipeChat.setOnRefreshListener(this);
        ChatUiHelper chatUiHelper = ChatUiHelper.with(getActivity());
        chatUiHelper.bindContentLayout(mContentLayout).bindEditText(mEdMsg).bindBottomLayout(mBottomLayout);
        mChatListView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom&&mChatListView!=null) {
                mChatListView.post(() -> mChatListView.scrollToPosition(0));
            }
        });
        mChatListView.setOnTouchListener((v, event) -> {
            chatUiHelper.hideBottomLayout(false);
            chatUiHelper.hideSoftInput();
            mEdMsg.clearFocus();
            return false;
        });
        mSendBtn.setOnClickListener(v -> {
            List<String> msgList = StringUtils.getStrList(mEdMsg.getText().toString(), 1000);
            for (String msg : msgList) {
                mViewModel.sendTextMessage(msg);
            }
            mEdMsg.setText("");
        });
        mViewModel.getMessageLiveData().observe(this, message -> {
            if (lastMsgTimeStamp == 0) {
                lastMsgTimeStamp = message.getSentTime();
            }
            mChatAdapter.addToStart(message);
            mChatListView.scrollToPosition(0);
        });
        mViewModel.getMessageListLiveData().observe(this, listReqResult -> {
            if (listReqResult.getStatus() == ConstantApp.STATUS_SUCCESS) {
                if (!listReqResult.getData().isEmpty()) {
                    lastMsgTimeStamp = listReqResult.getData().get(0).getSentTime();
                }
                Collections.reverse(listReqResult.getData());
                mChatAdapter.addData(totalMessage, listReqResult.getData());
                mSwipeChat.setRefreshing(false);
                mChatAdapter.resetLastShowTimeState();
                mChatListView.postDelayed(() -> {
                    mChatListView.smoothScrollToPosition(totalMessage);
                    totalMessage += listReqResult.getData().size();
                }, 200);
            }
        });
        mViewModel.getStatusLiveData().observe(this, msg -> {
            mChatAdapter.updateMsg(msg);
        });
        mViewModel.getGroupInfoLiveData().observe(this, groupInfo -> tvTitle.setText(groupInfo.getName()));
    }

    @Override
    public void requestData() {
        AR110Log.i(getTAG(), "requestData");
        mViewModel.getMyInfo(getContext());
        if (currentChatType == Enums.RecentContactType.PEOPLE_TYPE_VALUE) {
            tvDetail.setVisibility(View.GONE);
        }
        tvTitle.setText(mViewModel.getChatTitle(recentId, currentChatType));
        mViewModel.requestPageUp(REQUEST_MESSAGE_COUNT, lastMsgTimeStamp);
    }

    @Override
    public void onRefresh() {
        AR110Log.i(getTAG(), "onRefresh");
        mViewModel.requestPageUp(REQUEST_MESSAGE_COUNT, lastMsgTimeStamp);
    }

    private void updateModeUI() {
        if (currentMode == CHAT_TYPE_TEXT) {
            imgType.setImageDrawable(ActivityCompat.getDrawable(getContext(), R.drawable.chat_type_call));
            tvCall.setVisibility(View.GONE);
            mSendBtn.setVisibility(View.VISIBLE);
            mEdMsg.setVisibility(View.VISIBLE);
        } else if (currentMode == CHAT_TYPE_VIDEO) {
            imgType.setImageDrawable(ActivityCompat.getDrawable(getContext(), R.drawable.chat_type_text));
            tvCall.setVisibility(View.VISIBLE);
            mSendBtn.setVisibility(View.GONE);
            mEdMsg.setVisibility(View.GONE);
            mEdMsg.clearFocus();
        }
    }

    //回退
    private void back() {
        EmoticonsKeyboardUtils.closeSoftKeyboard(getContext());
        Activity act = getActivity();
        if (act != null) {
            NavigationHelper.Companion.getInstance().exitChat(this);
        }
    }

    @Override
    public void onBackPressed() {
        back();
//        return true;
    }
}
