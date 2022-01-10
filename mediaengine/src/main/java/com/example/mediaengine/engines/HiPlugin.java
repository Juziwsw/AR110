package com.example.mediaengine.engines;

import android.util.Log;

import com.example.mediaengine.interfaces.ICameraEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * 连接宿主与插件，支持宿主与插件间进行通讯
 */
public class HiPlugin {
    private final static String TAG = "HiPlugin";

    //将构造器设置为private禁止通过new进行实例化
    private HiPlugin() {
    }

    private final static class SingletonHolder {
        private static HiPlugin singletonPlugin = new HiPlugin();
    }

    public static HiPlugin getInstance() {
        return SingletonHolder.singletonPlugin;
    }

    private String version;
    private List<ICameraEngine> cameraList = new ArrayList<ICameraEngine>();
    private ICameraEngine screenCapture;
    private int cameraIndex = 0;

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public ICameraEngine getScreenCapture() {
        return screenCapture;
    }

    public void setScreenCapture(ICameraEngine screenCapture) {
        this.screenCapture = screenCapture;
    }

    public synchronized void registerCamera(ICameraEngine cameraEngine) {
        if (cameraEngine == null) return;
        Log.i(TAG, "registerCamera: " + cameraEngine.getInfo());
        if (!cameraList.contains(cameraEngine)) {
            Log.i(TAG, "addCamera: " + cameraEngine.getInfo());
            cameraList.add(cameraEngine);
        }
    }

    public synchronized void unRegisterCamera(ICameraEngine cameraEngine) {
        if (cameraEngine == null) return;
        Log.i(TAG, "unRegisterCamera: " + cameraEngine.getInfo());
        if (cameraList.contains(cameraEngine)) {
            Log.i(TAG, "removeCamera: " + cameraEngine.getInfo());
            cameraList.remove(cameraEngine);
        }
    }

    public synchronized ICameraEngine nextCameraEngine(boolean begin) {
        if (begin) {
            cameraIndex = 0;
        }
        ICameraEngine cameraEngine = null;
        if (cameraList.size() > cameraIndex) {
            cameraEngine = cameraList.get(cameraIndex);
        }
        cameraIndex++;
        if (cameraIndex >= cameraList.size()) {
            if (cameraList.size() == 0) {
                cameraIndex = 0;
            } else {
                cameraIndex = cameraIndex % cameraList.size();
            }
        }
        if (cameraEngine != null) {
            Log.i(TAG, "nextCameraEngine: " + cameraEngine.getInfo());
        }

        return cameraEngine;
    }

    public synchronized List<ICameraEngine> getCameraList() {
        return cameraList;
    }

    public synchronized void clearCamera() {
        cameraList.clear();
    }
}
