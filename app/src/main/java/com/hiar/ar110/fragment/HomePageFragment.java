package com.hiar.ar110.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hiar.ar110.R;
import com.hiar.ar110.activity.AR110MainActivity;
import com.hiar.ar110.base.BaseFragment;
import com.hiar.ar110.config.ModuleConfig;
import com.hiar.ar110.event.CommEventTag;
import com.hiar.ar110.event.EventLiveBus;
import com.hiar.ar110.helper.NavigationHelper;
import com.hiar.ar110.service.AR110BaseService;
import com.hiar.ar110.util.Util;
import com.hiar.ar110.viewmodel.HomeViewModel;
import com.hiar.mybaselib.utils.BitmapUtils;
import com.hileia.common.enginer.LeiaBoxEngine;

import java.io.File;
import java.util.Base64;

import androidx.constraintlayout.widget.ConstraintLayout;

import static android.view.View.GONE;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class HomePageFragment extends BaseFragment {
    private ConstraintLayout mLayoutJdList; //处警任务
    private ConstraintLayout mLayoutPatrol; //巡逻任务
    private ConstraintLayout mLayoutpopulation;//流动人口核查
    private ConstraintLayout mLayoutChatting; //合成作战
    private TextView mTextViewWellcom;
    private ImageView mImgSetting;
    private TextView mTextCopNotHandle;
    private TextView mTextCopHandling;
    private TextView mTextPatrolHandling;
    private TextView mTextPatrolHandled;
    private ConstraintLayout mLayoutCommuPhone = null;
    private HomeViewModel mViewModel;
    private TextView mTextVerificationHandling;
    private TextView mTextVerificationHandled;

    @Override
    public void onStart() {
        super.onStart();
        updateModuleConfig();
        if (AR110MainActivity.Companion.isPushing()) {
            mLayoutCommuPhone.setVisibility(View.VISIBLE);
        }
        refreshData();
    }

    public void refreshData() {
        mViewModel.getModuleConfig();
        mViewModel.getAppConfig();
        if (ModuleConfig.INSTANCE.isCopTaskEnable()) {
            mViewModel.refreshTaskNumber();
        }
        if (ModuleConfig.INSTANCE.isPatrolTaskEnable()) {
            mViewModel.refreshPatrolTaskNumber();
        }
        if (ModuleConfig.INSTANCE.isPopulationTaskEnable()) {
            mViewModel.refreshVerificationTaskNumber();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onBackPressed() {
        NavigationHelper.Companion.getInstance().makeActivityToBG();
    }

    @Override
    public void onHileiaComingCallCallback() {
        Activity act = getActivity();
        if (null != act) {
            act.runOnUiThread(() -> mLayoutCommuPhone.setVisibility(View.VISIBLE));
        }
    }

    @Override
    public void onHileiaHangupCallback() {
        Activity act = getActivity();
        if (null != act) {
            act.runOnUiThread(() -> mLayoutCommuPhone.setVisibility(GONE));
        }
    }

    public HomePageFragment() {
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_home_page;
    }

    @Override
    public void initData() {
    }

    @Override
    public void initView(View view) {
        mLayoutJdList = view.findViewById(R.id.layout_coptask);
        mLayoutpopulation = view.findViewById(R.id.layout_floating_task);
        mLayoutChatting = view.findViewById(R.id.layout_chatting_task);
        mLayoutCommuPhone = view.findViewById(R.id.layout_commu_phone);
        mLayoutPatrol = view.findViewById(R.id.layout_patrol_task);
        mTextViewWellcom = view.findViewById(R.id.text_wellcom);
        mImgSetting = view.findViewById(R.id.img_settings);
        mTextCopNotHandle = view.findViewById(R.id.text_not_handled);
        mTextCopHandling = view.findViewById(R.id.text_has_handled);
        mTextPatrolHandling = view.findViewById(R.id.text_patrol_handling);
        mTextPatrolHandled = view.findViewById(R.id.text_patrol_handled);
        mTextVerificationHandling = view.findViewById(R.id.text_floating_handling);
        mTextVerificationHandled = view.findViewById(R.id.text_floating_handled);
        mTextViewWellcom.setText(AR110BaseService.mUserInfo.name);
        //保存警员名字图片，添加视频水印使用
        Bitmap nameImage = BitmapUtils.getViewBitmap(mTextViewWellcom);
        if (nameImage != null) {
            BitmapUtils.saveBitmapToSDCard(nameImage, LeiaBoxEngine.getInstance().settingManager().getRootDir() + File.separator + "name.png", 100);
        }
        String userName = AR110BaseService.mUserInfo.name + ", 您好！";
        mTextViewWellcom.setText(userName);
        mViewModel = getViewModel(HomeViewModel.class);
    }

    @Override
    public void initListener() {
        mLayoutJdList.setOnClickListener(v -> {
            NavigationHelper.Companion.getInstance().backToJdList(false);

        });
        mLayoutpopulation.setOnClickListener(v -> {
            toPopulationVerification();
        });
        mLayoutChatting.setOnClickListener(v -> {
            //  进入聊天列表
            NavigationHelper.Companion.getInstance().beginRecentMsg();
        });
        mLayoutPatrol.setOnClickListener(v -> NavigationHelper.Companion.getInstance().backToPatrolList());
        mImgSetting.setOnClickListener(v -> NavigationHelper.Companion.getInstance().beginSettingsFragment());
    }

    @Override
    public void initObserver() {
        mViewModel.getRefreshTaskNumStatus().observe(this, result -> {
            mTextCopHandling.setText("处理中 " + result.processingTask);
            mTextCopNotHandle.setText("未处理 " + result.untreatedTask);
        });
        mViewModel.getRefreshPatrolTaskNumberStatus().observe(this, res -> {
            mTextPatrolHandling.setText("处理中 " + res.patrolStatusOutCount);
            mTextPatrolHandled.setText("已处理 " + res.patrolStatusCompleteCount);
        });
        mViewModel.getRefreshVerificationTaskNumberStatus().observe(this, res -> {
            mTextVerificationHandling.setText("处理中 " + res.verificationStatusOutCount);
            mTextVerificationHandled.setText("已处理 " + res.verificationStatusCompleteCount);
        });
        mViewModel.getHttpErrorMessage().observe(this, result ->{
            Util.showMessage(result.getMessage());
        });
        EventLiveBus.INSTANCE.getCommtEvent().observe(this, commEvent -> {
            if (commEvent.getTag() == CommEventTag.MODULE_CONFIG_UPDATE){
                updateModuleConfig();
            }else if(commEvent.getTag() == CommEventTag.DATA_UPDATE){
                refreshData();
            }
        });
    }

    private void toPopulationVerification() {
        NavigationHelper.Companion.getInstance().backPopulationVerification();
    }

    private void updateModuleConfig(){
        mLayoutJdList.setVisibility(ModuleConfig.INSTANCE.isCopTaskEnable()?View.VISIBLE: GONE);
        mLayoutPatrol.setVisibility(ModuleConfig.INSTANCE.isPatrolTaskEnable()?View.VISIBLE: GONE);
        mLayoutpopulation.setVisibility(ModuleConfig.INSTANCE.isPopulationTaskEnable()?View.VISIBLE: GONE);
        mLayoutChatting.setVisibility(ModuleConfig.INSTANCE.isChattingTaskEnable()?View.VISIBLE: GONE);
    }

    // 加密
    public static String Encrypt(String sSrc, String sKey) throws Exception {
        if (sKey == null) {
            System.out.print("Key为空null");
            return null;
        }
        // 判断Key是否为16位
        if (sKey.length() != 16) {
            System.out.print("Key长度不是16位");
            return null;
        }
        byte[] raw = sKey.getBytes("utf-8");
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");//"算法/模式/补码方式"
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes("utf-8"));

        return Base64.getEncoder().encodeToString(encrypted);//此处使用BASE64做转码功能，同时能起到2次加密的作用。
    }

}