package com.hiar.ar110.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.Utils;
import com.hiar.ar110.BuildConfig;
import com.hiar.ar110.R;
import com.hiar.ar110.base.BaseActivity;
import com.hiar.ar110.config.LoginConstants;
import com.hiar.ar110.util.Util;
import com.hiar.mybaselib.utils.AR110Log;
import com.hiscene.armap.TagMainActivity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

/**
 * author: liwf
 * date: 2020/5/27 12:39
 */
public class SplashActivity extends BaseActivity {
    private boolean mHasPermission = false;
    private static final int REQUEST_OVERLAY_DISPLAY = 1;
    private static final int REQUEST_EXTERNAL_STORAGE = 2;
    private static final int REQUEST_BACKGROUND_SERVICE = 3;
    private TextView mTextVersion;

    @Override
    public int getLayoutId() {
        return R.layout.activity_splash;
    }

    @Override
    public void initView() {
        mTextVersion = findViewById(R.id.version);
        String version = Util.getAPPVersionName(this);
        mTextVersion.setText("版本号:" + version);
    }

    @Override
    public void initData() {
        requestCameraPermission(new PermissionCallback() {
            @Override
            public void onSuccess() {
                startMainApp();
                finish();
            }

            @Override
            public void onFailure() {

            }
        });
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

    private interface PermissionCallback {
        void onSuccess();

        void onFailure();
    }

    private final String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            "android.permission.ACCESS_MOCK_LOCATION",
            "android.permission.FOREGROUND_SERVICE",
            "android.permission.CHANGE_WIFI_STATE",
            Manifest.permission.CHANGE_CONFIGURATION,
            Manifest.permission.WRITE_SETTINGS,
            "android.permission.FOREGROUND_SERVICE",
    };

    private final String[] permissionsRelease = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            "android.permission.ACCESS_MOCK_LOCATION",
            "android.permission.FOREGROUND_SERVICE",
            Manifest.permission.CHANGE_CONFIGURATION,
            "cybertech.permission.READ_PSTORE_USERINFO",
            "cybertech.permission.READ_PSTORE_LINKINFO",
            "cybertech.permission.READ_PSTORE_SERVER_SYNC_TIME",
            "cybertech.permission.READ_PSTORE_LOCATIONINFO",
            "cybertech.permission.READ_PSTORE",
            "cybertech.permission.WRITE_PSTORE_OPERATIONLOG",
            "cybertech.permission.READ_PSTORE_COMPONENT_AUTHORIZATION",
            "android.permission.FOREGROUND_SERVICE",
    };

    @TargetApi(23)
    private void requestCameraPermission(PermissionCallback callback) {
        if (Build.VERSION.SDK_INT >= 23) {
            int i = 0;
            mHasPermission = false;
            String[] permissionUsed = null;
            if (BuildConfig.DEBUG) {
                permissionUsed = permissions;
            } else {
                permissionUsed = permissionsRelease;
            }

            for (i = 0; i < permissionUsed.length; i++) {
                int permission = ActivityCompat.checkSelfPermission(SplashActivity.this, permissionUsed[i]);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SplashActivity.this, permissionUsed,
                            REQUEST_EXTERNAL_STORAGE);
                    break;
                }
            }

            if (i == permissionUsed.length) {
                mHasPermission = true;
                callback.onSuccess();
            } else {
                callback.onFailure();
            }
        } else {
            callback.onSuccess();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            int len = grantResults.length;
            for (int i = 0; i < len; i++) {
                if (PackageManager.PERMISSION_DENIED == grantResults[i]) {
                    if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION) ||
                            permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)
                    ) {
                        mHasPermission = false;
                        Toast.makeText(getApplicationContext(), "没有给AR110授权位置权限，请在系统设置中给AR110授权位置权限！", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }
            mHasPermission = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mHasPermission) {
            if (!isIgnoringBatteryOptimizations()) {
                requestIgnoreBatteryOptimizations();
                return;
            }

            if (needStartSetting) {
                boolean isSuccess = true;
                if (isHuawei()) {
                    isSuccess = goHuaweiSetting();
                } else if (isXiaomi()) {
                    isSuccess = goXiaomiSetting();
                } else if (isSamsung()) {
                    isSuccess = goSamsungSetting();
                } else if (isVIVO()) {
                    isSuccess = goVIVOSetting();
                } else if (isOPPO()) {
                    isSuccess = goOPPOSetting();
                }
                needStartSetting = false;
                if (isSuccess) {//如果自启动界面跳转失败就继续走下去，不影响后续逻辑
                    return;
                }
            }

            String tagShow = Util.getProperty("debug.show.tag", "disable");
            Intent intent;
            if (tagShow.equals("disable")) {
                startMainApp();
            } else {
                intent = new Intent(SplashActivity.this, TagMainActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }

    private void startMainApp() {
        long loginTime = Util.getLongPref(Utils.getApp(), LoginConstants.LoginTime, 0l);
        long timeDifference = (System.currentTimeMillis() - loginTime) / 1000;
        long timeValue = timeDifference / LoginConstants.TimeDifferenceComparisonValue;
        Log.i("TIME", "时间差 =" + timeValue);
        if (timeValue > LoginConstants.MAX_TimeDifference) {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        } else {
            startActivity(new Intent(SplashActivity.this, AR110MainActivity.class));
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isIgnoringBatteryOptimizations() {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(getPackageName());
        }
        return isIgnoring;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestIgnoreBatteryOptimizations() {
        try {
            AR110Log.i(TAG, "requestIgnoreBatteryOptimizations !!!");
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_BACKGROUND_SERVICE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean needStartSetting = false;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BACKGROUND_SERVICE) {
            if (resultCode == Activity.RESULT_OK) {
                AR110Log.i(TAG, "confirmed !!!");
                if (isHuawei()) {
                    needStartSetting = true;
                }
            }
        }

        if (requestCode == REQUEST_OVERLAY_DISPLAY) {
            if (resultCode == Activity.RESULT_OK) {
                AR110Log.i(TAG, "confirmed overlay !!!");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * 跳转到指定应用的首页
     */
    private boolean showActivity(@NonNull String packageName) {
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
            startActivity(intent);
            return true;
        } catch (Exception e) {
            AR110Log.e(TAG, "showActivity Exception", e);
            return false;
        }
    }

    /**
     * 跳转到指定应用的指定页面
     * return 是否跳转成功
     */
    private boolean showActivity(@NonNull String packageName, @NonNull String activityDir) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packageName, activityDir));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        } catch (Exception e) {
            AR110Log.e(TAG, "showActivity Exception", e);
            return false;
        }

    }

    public boolean isHuawei() {
        if (Build.BRAND == null) {
            return false;
        } else {
            return Build.BRAND.toLowerCase().equals("huawei") || Build.BRAND.toLowerCase().equals("honor");
        }
    }

    /**
     * @return 是否启动成功
     */
    private boolean goHuaweiSetting() {
        AR110Log.i(TAG, "goHuaweiSetting show StartupNormalAppListActivity");
        boolean isSuccess = showActivity("com.huawei.systemmanager",
                "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity");
        if (!isSuccess) {
            isSuccess = showActivity("com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.bootstart.BootStartActivity");
        }
        return isSuccess;
    }

    public static boolean isXiaomi() {
        return Build.BRAND != null && Build.BRAND.toLowerCase().equals("xiaomi");
    }

    private boolean goXiaomiSetting() {
        AR110Log.i(TAG, "goXiaomiSetting !!!");
        return showActivity("com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity");
    }

    public static boolean isOPPO() {
        return Build.BRAND != null && Build.BRAND.toLowerCase().equals("oppo");
    }

    private boolean goOPPOSetting() {
        boolean isSuccess = showActivity("com.coloros.phonemanager");
        if (!isSuccess) {
            isSuccess = showActivity("com.oppo.safe");
            if (!isSuccess) {
                isSuccess = showActivity("com.coloros.oppoguardelf");
                if (!isSuccess) {
                    isSuccess = showActivity("com.coloros.safecenter");
                }
            }
        }
        return isSuccess;
    }

    public static boolean isVIVO() {
        return Build.BRAND != null && Build.BRAND.toLowerCase().equals("vivo");
    }

    private boolean goVIVOSetting() {
        return showActivity("com.iqoo.secure");
    }

    public static boolean isSamsung() {
        return Build.BRAND != null && Build.BRAND.toLowerCase().equals("samsung");
    }

    private boolean goSamsungSetting() {
        boolean isSuccess = showActivity("com.samsung.android.sm_cn");
        if (!isSuccess) {
            isSuccess = showActivity("com.samsung.android.sm");
        }
        return isSuccess;
    }
}
