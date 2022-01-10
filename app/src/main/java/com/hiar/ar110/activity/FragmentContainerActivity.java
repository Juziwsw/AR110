package com.hiar.ar110.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.hiar.ar110.R;
import com.hiar.ar110.base.BaseActivity;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/**
 * Author:wilson.chen
 * date：5/12/21
 * desc：
 */
public class FragmentContainerActivity extends BaseActivity {

    public static final void start(Context context, String fname, Bundle args) {
        Intent intent = new Intent(context, FragmentContainerActivity.class);
        intent.putExtra("fname", fname);
        intent.putExtra("args", args);
        context.startActivity(intent);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_fragment_container;
    }

    @Override
    public void initView() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("fname")) {
                showFragment(intent.getStringExtra("fname"), intent.getBundleExtra("args"));
            } else {
                finish();
            }
        }
    }


    @Override
    public void initData() {

    }

    @Override
    public void initListener() {

    }

    @Override
    public void onBrightNessChange(boolean inPackage) {

    }

    @Override
    public boolean needFinish() {
        return false;
    }

    public void showFragment(String fname, Bundle args) {
        showFragment(Fragment.instantiate(this, fname, args), true);

    }

    protected void showFragment(Fragment fragment, boolean replaceMode) {
        if (fragment == null) return;
        hideInput();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment oldFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (oldFragment != null) {
            fragmentTransaction.hide(oldFragment);

        }
        String tag = fragment.getClass().getName();
        if (oldFragment != null && tag.equals(oldFragment.getClass().getName())) {
            return;
        }
        if (replaceMode) {
            fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
        } else {
            fragmentTransaction.add(R.id.fragment_container, fragment, tag);
            fragmentTransaction.addToBackStack(tag);
        }
        fragmentTransaction.commit();
    }
}
