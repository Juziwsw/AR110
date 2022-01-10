package com.hiar.ar110.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.widget.RelativeLayout;

import com.blankj.utilcode.util.Utils;
import com.hiar.ar110.R;
import com.hiar.ar110.activity.AR110MainActivity;
import com.hiar.ar110.adapter.JDListAdapter;
import com.hiar.ar110.base.BaseFragment;
import com.hiar.ar110.helper.NavigationHelper;
import com.hiar.ar110.service.AR110BaseService;
import com.hiar.ar110.util.Util;
import com.hiar.ar110.viewmodel.JDListViewModel;
import com.hiar.ar110.widget.SpaceDecoration;
import com.hiar.mybaselib.utils.AR110Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.view.View.GONE;

/**
 * 警单列表
 */
public class JDListFragment extends BaseFragment {
    private RecyclerView mRecyclerView;
    private JDListAdapter mAdapter;
    private RelativeLayout mLayoutBack;
    private RelativeLayout mLayoutCommuPhone;
    private JDListViewModel mJDListViewModel;
    private int lastLoadDataItemPosition = 0;
    private int firstVisibleItem = 0;
    private boolean mHasOverlayPermission = false;

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //SYSTEM_ALERT_WINDOW权限申请
            if (!Settings.canDrawOverlays(Utils.getApp())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + Utils.getApp().getPackageName()));//不加会显示所有可能的app
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, 1);
            } else {
                //TODO do something you need
            }
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_j_d_list;
    }

    @Override
    public void initData() {
        mJDListViewModel = getViewModel(JDListViewModel.class);
    }

    @Override
    public void initView(View contentView) {
        mLayoutBack = contentView.findViewById(R.id.layout_back);
        mRecyclerView = contentView.findViewById(R.id.recycler_view);
        mAdapter = new JDListAdapter(getActivity());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new SpaceDecoration(0,
                (int) getResources().getDimension(R.dimen.dp12)));
        mRecyclerView.setAdapter(mAdapter);
        mLayoutCommuPhone = contentView.findViewById(R.id.layout_commu_phone);
    }

    @Override
    public void initListener() {
        mLayoutBack.setOnClickListener(v -> NavigationHelper.Companion.getInstance().beginHomePage());
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                //存在问题 数据不超过一屏，一直都会进入刷新方法
                if (newState == RecyclerView.SCROLL_STATE_IDLE &&
                        firstVisibleItem == 0) {
                    Util.mNewMsgCjdbh2 = null;
                    //AR110Log.i_JDLIST("touch","SCROLL_STATE_IDLE !");
                    mJDListViewModel.refreshJD(true);
                    return;
                }

                if (newState == RecyclerView.SCROLL_STATE_IDLE &&
                        lastLoadDataItemPosition == mAdapter.getItemCount()) {
                    if (!mJDListViewModel.ismGetFull()) {
                        mJDListViewModel.refreshJD(false);
                    }
                    return;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager instanceof LinearLayoutManager) {
                    LinearLayoutManager manager = (LinearLayoutManager) layoutManager;
                    firstVisibleItem = manager.findFirstVisibleItemPosition();
                    int l = manager.findLastCompletelyVisibleItemPosition();
                    lastLoadDataItemPosition = firstVisibleItem + (l - firstVisibleItem) + 1;
                }
            }
        });

        mJDListViewModel.getCopTaskRecord().observe(this, copTaskRecords -> {

            if (null != mAdapter) {
                Activity act = getActivity();
                if (act != null) {
                    AR110Log.i(getTAG(), "begin refresh list, mJdlist size=" + copTaskRecords.size());
                    mAdapter.setAdapter(copTaskRecords);
                }
            } else {
                AR110Log.i(getTAG(), "mAdapter is null");
            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (AR110BaseService.mUserInfo != null) {
            mJDListViewModel.refreshJD(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (AR110MainActivity.Companion.isPushing()) {
            mLayoutCommuPhone.setVisibility(View.VISIBLE);
        }

        if (!mHasOverlayPermission) {
            mHasOverlayPermission = true;
            checkPermission();
        }

    }

    @Override
    public void onPause() {
        AR110Log.i(getTAG(), "onPause !!!");
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        AR110Log.i(getTAG(), "onDestroy !!!");
        super.onDestroy();
    }

    @Override
    public void onHileiaComingCallCallback() {
        Activity act = getActivity();
        if (act != null) {
            act.runOnUiThread(() -> mLayoutCommuPhone.setVisibility(View.VISIBLE));
        }
    }

    @Override
    public void onHileiaHangupCallback() {
        Activity act = getActivity();
        if (act != null) {
            act.runOnUiThread(() -> mLayoutCommuPhone.setVisibility(GONE));
        }
    }

    @Override
    public void onBackPressed() {
        NavigationHelper.Companion.getInstance().backToHomePage(this);
//        return false;
    }
}
