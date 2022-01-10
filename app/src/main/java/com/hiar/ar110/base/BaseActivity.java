package com.hiar.ar110.base;

import android.app.Dialog;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    protected String TAG = getClass().getSimpleName();
    private Dialog mDialog = null;
    public static boolean mIsInPackage = false;
    protected boolean mForcePhoneUi = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager
        // .LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(getLayoutId());
        initialize();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initialize() {
        initView();
        initData();
        initListener();
    }

    public abstract int getLayoutId();

    public abstract void initView();

    public abstract void initData();

    public abstract void initListener();

    public abstract void onBrightNessChange(boolean inPackage);

    public abstract boolean needFinish();

    /**
     * 显示键盘
     *
     * @param et 输入焦点
     */
    public void showInput(final EditText et) {
        et.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(et, 0);
    }

    /**
     * 隐藏键盘
     */
    protected void hideInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View v = getWindow().peekDecorView();
        if (null != v) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public void setBrightness(int brightness) {
        if (brightness == 0) {
            onBrightNessChange(true);
        } else {
            onBrightNessChange(false);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    private Toast mToast = null;

    public void showMessage(String msg) {
        if (mToast != null) {
            mToast.setText(msg);
            mToast.setDuration(Toast.LENGTH_SHORT);
            mToast.show();
        } else {
            mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
            mToast.show();
        }
    }

    public void showMessage(int id) {
        if (mToast != null) {
            mToast.setText(getString(id));
            mToast.setDuration(Toast.LENGTH_SHORT);
            mToast.show();
        } else {
            mToast = Toast.makeText(this, getString(id), Toast.LENGTH_SHORT);
            mToast.show();
        }
    }
}
