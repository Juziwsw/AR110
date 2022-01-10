package com.hiar.sdk.face;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.Log;

import com.hiar.sdk.face.callback.RecognitionResultCallBack;
import com.hiar.sdk.face.request.FaceRecognizeRequest;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FaceEngine {
    private static final String TAG = "FaceEngine";

    static {
        System.loadLibrary("hiarface_engine");
    }

    public static Lock lock_db = new ReentrantLock();


    public static class FaceDetectionInfo {
        public Rect bbox;             // 人脸框left<=right,top<=bottom
        public long frame_id;         // 视频帧id
        public long face_id;          // 人脸id

        public FaceDetectionInfo() {
            bbox = new Rect(0, 0, 0, 0);
            face_id = 0;
        }
    }

    // sdk 版本信息
    public class VersionInfo{
        public int major;                      // "2" in "2.4.7"
        public int minor;                      // "4" in "2.4.7"
        public int patch;                      // "7" in "2.4.7"
        public int build;                      // reserved
        public String description;             // description of this version
    }

    FaceRecognizeRequest request = new FaceRecognizeRequest();
    public RecognitionResultCallBack recognitionResultCallBack;
    /**
     * 获得识别结果的回调函数，将结果存放到人脸识别队列中。
     *
     * @param face_id             [input] 人脸id
     * @param frame_id            [input] 图像帧id
     * @param x_min               [input] 人脸框水平最左端
     * @param y_min               [input] 人脸框竖直最上端
     * @param x_max               [input] 人脸框水平最右端
     * @param y_max               [input] 人脸框竖直最下端
     * @param user_id             [input] 注册用户id
     * @param similarity          [input] 相似度，取值[0,1.0],0最不相似，1.0最相似
     * @return void
     */
    public void onNativeCallBackRecognize(long face_id, long frame_id, int x_min,
                                          int y_min, int x_max, int y_max, long user_id,  float similarity, byte[] feature) throws Exception {
        request.send(recognitionResultCallBack, frame_id, face_id, user_id, similarity);
        int box_width = x_max - x_min;
        int box_height = y_max - y_min;

        Log.i(TAG, "人脸识别回调函数输出人脸识别结果" + ": user_id = " + user_id + " similarity = "
                + similarity + " frame_id = " + frame_id + " face_id = " + face_id + " width = " + box_width +"height = " + box_height + "\n");
        return;
    }

    private static FaceEngine faceEngine;
    private static boolean bInit = false;
    private static boolean boutput = false;
    private static int num_wait_time = 0;
    private static int WAIT_TIME_MAX = 3;

    private static long handle = 0; // 保存sdk实例化后的句柄

    private static long frame_ID = 0;

    public synchronized static FaceEngine getInstance() {

        if (faceEngine == null) {
            faceEngine = new FaceEngine();
        }
        return faceEngine;

    }

    public synchronized void stopFaceEngineOutput() {

        boutput = false;
    }

    public synchronized void startFaceEngineOutput() {

        num_wait_time = 0;
    }


    public synchronized boolean initFaceEngine(Context context, String log_path) {

        if (bInit) {
            return true;
        }

        boutput = true;

        if (log_path == null) {
            log_path = " ";
        }

        log_path = "";

        long[] handle_ = new long[1];
        bInit = nativeHiarFaceModelInit(handle_, log_path, (Activity)context);
        if (bInit) {
            Log.i(TAG, "initFaceEngine: success");
            handle = handle_[0];
        }
        else
        {
            Log.i(TAG, "initFaceEngine: failure ");
        }

        return bInit;

    }


    // 重载,实现人脸检测
    public FaceDetectionInfo[] run(byte[] imageDate, int imageWidth, int imageHeight, int imageFormat) {
        return run(imageDate,imageWidth,imageHeight,imageFormat,false);
    }

    /**
     * 基于连续图像序列或视频帧或摄像头数据帧的人脸检测与人脸识别，人脸检测结果同步输出，
     * 人脸识别结果通过回调函数（onNativeCallBackRecognize）异步输出。
     *
     * @param imageDate           [input] 输入图像数据，支持YUV21或YUV12
     * @param imageWidth          [input] 图像宽度
     * @param imageHeight         [input] 图像高度
     * @param imageFormat         [input] 取值为12或21，12代表YUV12,21代表YUV21
     * @param flagRecognition     [input] 是否要执行人脸识别程序，true执行人脸识别程序，false不执行人脸识别程序
     * @return FaceDetectionInfo[]   [output] 返回检测到的人脸框
     */
    public FaceDetectionInfo[] run(byte[] imageDate, int imageWidth, int imageHeight, int imageFormat, boolean flagRecognition) {

        if (bInit == false) {
            return null;
        }

        // Log.i(TAG, "~~~画人脸框########################~~~");
        ++frame_ID; // 更新输入图像帧ID

        // flagRecognition = false; //关闭人脸识别

        FaceBBoxEntity[] faceEntity = null;
        try{
            faceEntity = nativeFaceRun(handle, imageDate, imageWidth, imageHeight, imageFormat, frame_ID, flagRecognition);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        if (faceEntity == null) {
            return null;
        }

        int faces_num = faceEntity.length;
        if (faces_num > 0) {
            int face_size =  faceEntity[0].x_max -  faceEntity[0].x_min;
            Log.i(TAG, "~~~画人脸框#########Size,X1,Y1,X2,Y2~~~" +face_size+", "+ faceEntity[0].x_min + "," + faceEntity[0].y_min + "," + faceEntity[0].x_max + "," + faceEntity[0].y_max);
        }

        FaceDetectionInfo[] faceInfos = null;
        if (faces_num > 0) {
            faceInfos = new FaceDetectionInfo[faces_num];
            for (int i = 0; i < faces_num; i++) {
                faceInfos[i] = new FaceDetectionInfo();
                faceInfos[i].bbox.left = faceEntity[i].x_min;
                faceInfos[i].bbox.top = faceEntity[i].y_min;
                faceInfos[i].bbox.right = faceEntity[i].x_max;
                faceInfos[i].bbox.bottom = faceEntity[i].y_max;
                faceInfos[i].face_id = faceEntity[i].face_id;
                faceInfos[i].frame_id = frame_ID;
            }
        }

        if(!boutput)
        {
            num_wait_time++;
            if(num_wait_time%WAIT_TIME_MAX==0)
            {
                boutput = true;
            }
            return null;
        }

        return faceInfos;
    }


    /**
     * 释放人脸引擎
     * @return boolean            [output] true 释放成功，false 释放失败
     */
    public synchronized boolean releaseFaceEngine() {
        Log.i(TAG, "releaseFaceEngine: ");
        if (!bInit) {
            return true;
        }

        Log.i(TAG, "releaseFaceEngine: nativeRelease()");
        boolean bret = false;
        try{
            bret = nativeRelease(handle);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if (bret) {
            bInit = false;
            handle = 0;
            Log.i(TAG, "releaseFaceEngine: success ");
            return true;
        }
        else
        {
            Log.i(TAG, "releaseFaceEngine: failure ");
            return false;
        }

    }

    /**
     * 通过图像文件名提取图像中包含的最大人脸的特征
     *
     * @param path_img            [input] 包含人脸的图像路径
     * @param path_crop           [input] 人脸小图像的输出路径，可以为"",表示不输出人脸小图像
     * @param feature             [output] 人脸特征 长度为512*4
     * @return boolean            [output] true提取人脸特征成功，false提取人脸特征失败
     */
    public boolean getFeatureByImageFile(String path_img, String path_crop, byte[] feature) {
        if (!bInit) {
            return false;
        }
        if (handle != 0) {
            try{
                return nativeGetFeatureByImageFile(handle, path_img, path_crop, feature);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 通过摄像头读取的图像数据进行人脸特征提取
     *
     * @param yuvData            [input] 从摄像头读取的图像数据
     * @param width              [input] 图像宽度
     * @param height             [input] 图像高度
     * @param type               [input] 图像数据格式，12代表YUV12；21 代表YUV21
     * @param path_crop          [input] 人脸小图像的输出路径，可以为"",表示不输出人脸小图像
     * @param feature            [output] 人脸特征 长度为512*4
     * @return boolean           [output] true提取人脸特征成功，false提取人脸特征失败
     */
    public boolean getFeatureByImageData(byte[] yuvData, int width, int height, int type, String path_crop, byte[] feature) {
        if (!bInit) {
            return false;
        }
        if (handle != 0) {
            try{
                return nativeGetFeatureByYUV420(handle, yuvData, width, height, type, path_crop, feature);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 特征查询
     *
     * @param feature           [input] 输入特征,大小为2048的byte
     * @param top_n             [input] 输入匹配值返值最相似的 top n个人脸ID，取值为1到10，[1,10]
     * @param user_ids          [output] 输出已匹配的用户ID，该ID不限于原始数据库中的用户ID
     * @param user_similaritys  [output] 输出匹配的相似度,取值在[0,1.0],0表示最不相似，1.0表示最相似
     * @return int ret          [output] 查找到最相似的前ret个人脸
     */
    public int searchFeature(byte[] feature, int top_n, long[] user_ids, float[] user_similaritys)
    {
        if (!bInit) {
            return 0;
        }
        if (handle != 0) {
            FeatureEntity featureEntity = new FeatureEntity();
            featureEntity.data = feature;
            featureEntity.user_id = 0;
            try{
                return nativeSearchFeature(handle, featureEntity, top_n, user_ids, user_similaritys);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return 0;
    }


    /**
     * 特征匹配
     *
     * @param featureA         [input] 输入特征 A
     * @param featureB         [input] 输入特征 B
     * @return float ret       [output] 返回匹配的相似度,取值在[0,1.0],0表示最不相似，1.0表示最相似
     */
    public float matchFeature(byte[] featureA, byte[] featureB)
    {
        if (!bInit) {
            return 0;
        }
        if (handle != 0) {
            FeatureEntity featureEntityA = new FeatureEntity();
            featureEntityA.data = featureA;
            FeatureEntity featureEntityB = new FeatureEntity();
            featureEntityB.data = featureB;
            try{
                return nativeMatchFeature(handle, featureEntityA, featureEntityB);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * 根据注册用户user_id,在内部特征库中增加人脸特征
     * @param user_id           [input] 用户 id
     * @param feature           [input] 人脸特征
     * @return boolean          [output] true 增加成功，false 增加失败
     */
    public boolean addFaceFeature(long user_id, byte[] feature) {
        lock_db.lock();
        boolean bret = false;
        if (handle != 0 && bInit) {
            FeatureEntity[] featureEntity = new FeatureEntity[1];
            featureEntity[0] = new FeatureEntity();
            featureEntity[0].user_id = user_id;
            featureEntity[0].data = feature;
            // System.arraycopy(feature);

            try{
                int add_num = nativeAddUserFeature(handle, featureEntity);
                if (add_num > 0) {
                    bret = true;
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }

        }

        lock_db.unlock();

        return bret;
    }

    /**
     * 根据user_Id删除注册人脸
     *
     * @param user_id           [input] 用户 id
     * @return boolean          [output] true 删除成功，false 删除失败
     */
    public boolean deleteFaceFeature(int user_id) {
        lock_db.lock();
        boolean bret = false;
        if (handle != 0 && bInit) {
            long[] arr_id = new long[1];
            arr_id[0] = user_id;
            try{
                int del_num = nativeDelUserFeature(handle, arr_id);
                if (del_num > 0) {
                    bret = true;
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        lock_db.unlock();
        return bret;
    }

    /**
     * 清除所有注册人脸
     *
     * @return boolean           [output] true清除所有注册人脸信息成功，false 清除所有注册人脸信息失败
     */
    public boolean clearFaceFeature() {
        boolean bret = false;
        lock_db.lock();
        if (handle != 0 && bInit) {
            try{
                bret = nativeClearUserFeature(handle);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        lock_db.unlock();
        return bret;
    }

    /**
     * 将注册人脸特征信息传递给算法
     *
     * @param faceEntityList     [input] 注册人脸信息数组
     * @return boolean           [output] true 推送信息成功，false推送信息失败
     */
    public boolean pushFaceFeature(List<FaceEntity> faceEntityList) {

        lock_db.lock();
        boolean bret = false;
        if (handle != 0 && bInit) {
            FeatureEntity[] featureEntitys = new FeatureEntity[faceEntityList.size()];
            for (int i = 0; i < faceEntityList.size(); i++) {
                featureEntitys[i] = new FeatureEntity();
                featureEntitys[i].data = faceEntityList.get(i).getFeature();
                featureEntitys[i].user_id = i + 1;
            }

            try {
                bret = nativePushUserFeature(handle, featureEntitys) >= 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        lock_db.unlock();
        return bret;
    }


    public VersionInfo getVersion() {
        VersionInfo versionInfo = new VersionInfo();
        nativeGetVersion(versionInfo);
        return versionInfo;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // face engine
    // 以下是人脸引擎接口，未经HiAR许可，禁止修改

    // 人脸外接矩形框
    private class FaceBBoxEntity {
        public long face_id;                     // 人脸的标识符，用于区别临时跟踪的人脸框
        public long frame_id;                    // 当前人脸框出现的帧ID
        public int x_min,y_min, x_max, y_max;    //人脸框的位置，左上角坐标为(x_min,y_min)、右下角坐标为(x_max,y_max)

        public FaceBBoxEntity() {
            face_id = 0;
            frame_id = 0;
            x_min = 0;
            y_min = 0;
            x_max = 0;
            y_max = 0;
        }
    }

    //人脸特征描述
    private class FeatureEntity {
        public long user_id;                    // 人脸身份id，表示该人脸属于身份为id的注册用户
        public byte[] data;                     // 特征向量为2048个字节

        public FeatureEntity() {
            user_id = 0;
            data = new byte[512 * 4];
        }
    }


    /**
     * 初始化人脸引擎
     *
     * @param handle             [output] 人脸引擎句柄
     * @param path_log           [input] 保存日志路径，默认路径在/sdcard/hiar_face/logger/log.txt
     * @param activity
     * @return true              [output] 初始化成功，false 初始化失败
     */
    private native boolean nativeHiarFaceModelInit(long[] handle, String path_log, Activity activity);

    /**
     * 基于连续图像序列或视频帧或摄像头数据帧的人脸检测与人脸识别，人脸检测结果同步输出，
     * 人脸识别结果通过回调函数（onNativeCallBackRecognize）异步输出。
     *
     * @param handle              [input] 人脸引擎句柄
     * @param imageDate           [input] 输入图像数据，支持YUV21或YUV12
     * @param imageWidth          [input] 图像宽度
     * @param imageHeight         [input] 图像高度
     * @param imageFormat         [input] 取值为12或21，12代表YUV12,21代表YUV21
     * @param frameID             [input] 每帧图像的ID
     * @param flagRecognition     [input] 是否要执行人脸识别程序，true执行人脸识别程序，false不执行人脸识别程序
     * @return FaceBBoxEntity[]   [output] 返回检测到的人脸框
     */
    private native FaceBBoxEntity[] nativeFaceRun(long handle, byte[] imageDate, int imageWidth, int imageHeight, int imageFormat, long frameID, boolean flagRecognition);

    /**
     * 释放人脸引擎占用资源
     *
     * @param handle              [input] 人脸引擎句柄
     * @return boolean            [output] true资源释放成功，false资源释放失败
     */
    private native boolean nativeRelease(long handle);

    /**
     * 通过图像文件名提取图像中包含的最大人脸的特征
     *
     * @param handle              [input] 人脸引擎句柄
     * @param path_img            [input] 包含人脸的图像路径
     * @param path_crop           [input] 人脸小图像的输出路径，可以为"",表示不输出人脸小图像
     * @param feature             [output] 人脸特征 长度为512*4
     * @return boolean            [output] true提取人脸特征成功，false提取人脸特征失败
     */
    private native boolean nativeGetFeatureByImageFile(long handle, String path_img, String path_crop, byte[] feature);

    /**
     * 通过摄像头读取的图像数据进行人脸特征提取
     *
     * @param handle             [input] 人脸引擎句柄
     * @param imageDate          [input] 从摄像头读取的图像数据
     * @param imageWidth         [input] 图像宽度
     * @param imageHeight        [input] 图像高度
     * @param imageFormat        [input] 图像数据格式，12代表YUV12；21 代表YUV21
     * @param path_crop          [input] 人脸小图像的输出路径，可以为"",表示不输出人脸小图像
     * @param feature            [output] 人脸特征 长度为512*4
     * @return boolean           [output] true提取人脸特征成功，false提取人脸特征失败
     */
    private native boolean nativeGetFeatureByYUV420(long handle, byte[] imageDate, int imageWidth, int imageHeight, int imageFormat, String path_crop, byte[] feature);

    //feature database
    /**
     * 人脸物征集推送到人脸引擎中，以便人脸识别，用输入人脸集信息代替换历史人脸集特征信息
     *
     * @param handle             [input] 人脸引擎句柄
     * @param arrayFeature       [input] 人脸特征与用户信息
     * @return int               [output] 成功推送了人脸特征数
     */
    private native int nativePushUserFeature(long handle, FeatureEntity[] arrayFeature);


    /**
     * 在人脸引擎内部人脸物征集上增加新的人脸特征及用户信息
     *
     * @param handle             [input] 人脸引擎句柄
     * @param arrayFeature       [input] 人脸特征与用户信息
     * @return boolean           [output] true 增加成功，false 增加失败
     */
    private native int nativeAddUserFeature(long handle, FeatureEntity[] arrayFeature);


    /**
     * 在人脸引擎内部人脸物征集上删除的人脸特征及用户信息
     *
     * @param handle             [input] 人脸引擎句柄
     * @param user_ids           [input] 用户id
     * @return boolean           [output] true 删除成功，false 删除失败
     */
    private native int nativeDelUserFeature(long handle, long[] user_ids);


    /**
     * 清除人脸引擎内部人脸物征集
     *
     * @param handle             [input] 人脸引擎句柄
     * @return boolean           [output] true 清除成功，false 清除失败
     */
    private native boolean nativeClearUserFeature(long handle);


    /**
     * 特征查询
     *
     * @param handle            [input] 人脸引擎句柄
     * @param feature           [input] 输入特征
     * @param top_n             [input] 输入匹配值返值最相似的 top n个人脸ID，取值为1到10，[1,10]
     * @param user_ids          [output] 输出已匹配的用户ID，该ID不限于原始数据库中的用户ID
     * @param user_similaritys  [output] 输出匹配的相似度,取值在[0,1.0],0表示最不相似，1.0表示最相似
     * @return int ret          [output] 查找到最相似的前ret个人脸
     */
    private native int nativeSearchFeature(long handle, FeatureEntity feature, int top_n, long[] user_ids, float[] user_similaritys);


    /**
     * 特征匹配
     *
     * @param handle           [input] 人脸引擎句柄
     * @param featureA         [input] 输入特征 A
     * @param featureB         [input] 输入特征 B
     * @return float ret       [output] 返回匹配的相似度,取值在[0,1.0],0表示最不相似，1.0表示最相似
     */
    private native float nativeMatchFeature(long handle, FeatureEntity featureA, FeatureEntity featureB);


    /**
     * 获得SDK版本信息
     * @param versionInfo      [output] 人脸SDK版本信息
     */
    private native void nativeGetVersion(VersionInfo versionInfo);

}

