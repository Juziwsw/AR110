package com.hiar.ar110.util;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.hiar.ar110.BuildConfig;
import com.hiar.ar110.config.LoginConstants;
import com.hiar.ar110.config.ModuleConfig;
import com.hiar.ar110.data.Constants;
import com.hiar.ar110.data.FileUploadData;
import com.hiar.ar110.data.UserInfoData;
import com.hiar.ar110.data.VideoUploadState;
import com.hiar.ar110.data.audio.AudioUploadStatus;
import com.hiar.ar110.data.photo.PhotoUploadStatus;
import com.hiar.mybaselib.utils.AR110Log;
import com.hiar.mybaselib.utils.toast.ToastUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import cn.com.cybertech.models.User;
import cn.com.cybertech.pdk.UserInfo;

public class Util {
    public static boolean mNeedMultiScreen = true;

    public enum CompileVersion {
        POLICE_PRODUCTION,      //公安网发布版本
        POLICE_DEVELOP,   //公安网测试版本
        HISCENE_EXTERNAL, //亮风台公网演示版本
        HISCENE_DEVELOP,  //亮风台内外映射到外网
        INTERNAL_DEVELOP,   // 内网测试
        DEBUG_INTERNAL_NEW,   //公司内网测试环境
        ZHAOTONG //昭通

    }

    private static CompileVersion compileVERSION = CompileVersion.DEBUG_INTERNAL_NEW;

    public static boolean mISExternalWeb = false;
    public static String mExternal_URL = "http://106.75.214.60:10177";
    private static final String TAG = "Util";

    public static String mExternalHileiaUrl = "106.75.214.60";
    public static String mExternalHileiaPort = "30177";

    //内部debug使用
   /* public static String mDebugBaseIp = "192.168.103.63";
    public static String mBaseIp = "http://" + mDebugBaseIp + ":8299";*/

   /*//Hileia开发环境IP端口
    private static final String HILEIA_PRIVATE_DEBUG_URL = "192.168.18.132";
    private static final String HILEIA_PRIVATE_DEBUG_PORT = "30134";

    //开发环境IP
    public static String mDebugBaseIp = "192.168.18.132";
    //开发环境IP端口
    public static String mBaseIp = "http://" + mDebugBaseIp + ":30171";*/

    //Hileia测试环境IP，端口
    private static String HILEIA_PRIVATE_DEBUG_URL = "112.65.179.30";
    private static String HILEIA_PRIVATE_DEBUG_PORT = "30177";

    //测试环境IP
    public static String mDebugBaseIp = "112.65.179.30";
    //测试环境IP端口
    public static String mBaseIp = "http://" + mDebugBaseIp + ":30174";

    private static final String HILEIA_PRIVATE_URL = "20.65.2.12";

    //旧版hiliea
    /*private static final String HILEIA_PRIVATE_PORT = "4089";*/

    //Hileia测试环境
    //private static final String HILEIA_PRIVATE_PORT = "4124";

    //Hileia 82服务器
    private static String HILEIA_PRIVATE_PORT = "4156";

    //旧版本公安环境
    //public static String mBaseIpPolice = "http://20.65.2.12:4084";

    //公安网测试环境
    //public static String mBaseIpPolice = "http://20.65.2.12:4130";

    //公安网82服务器
    public static String mBaseIpPolice = "http://20.65.2.12:4136";
    //mqtt开发环境
    public static String mMqttHost = "tcp://192.168.23.65:31885";

    public static String mFaceLibBaseUrl = "http://20.65.2.12:4088";
    public static String mFtpIp = "20.65.2.12";
    public static int mFtpPort = 4087;

    public static String mCarPhotoUrlHead = "http://112.65.179.30:30160/";
    public static String mFacePhotoUrlHead = "http://112.65.179.30:30160/";
    public static String mPicturePhotoUrlHead = "http://112.65.179.30:30160/";

    public static final int MAX_FACE_THRESHOOD = 95;
    public static final int DEF_FACE_THRESHOOD = 90;
    public static final int MIN_FACE_THRESHOOD = 80;

    public static final int MAX_LPR_THRESHOOD = 99;
    public static final int DEF_LPR_THRESHOOD = 93;
    public static final int MIN_LPR_THRESHOOD = 80;

    public static final int COPTASK_SOUND_SET_ALL = 1;
    public static final int PATROL_SOUND_SET_IMPORTANT = 2;

    public static final int DEF_CAR_RECOG_MODE = 1;

    public static final int PREVIEW_WIDTH = 1280;
    public static final int PREVIEW_HEIGHT = 720;

    public static final int RECOG_STATUS_NOTINLIB = 1;
    public static final int RECOG_STATUS_INLIB = 2;

    //测试IP是否接通
    public static final String mCheckIpUrl = "/api/police/v0.1/test/test";
    //获取警单列表
    private static final String mCopTaskRecord = "/api/police/v0.1/record/searchCopTask";
    //警单模式，人脸识别上传人脸图片
    public static final String mFaceUploadUrl = "/api/police/v0.1/live/addFace";
    //巡逻模式，人脸识别上传人脸图片
    public static final String mPatrolAddFace = "/api/police/v0.1/patrol/face/add";
    //警单模式，上传GPS位置信息
    public static final String mLocationUrl = "/api/police/v0.1/live/addNode";
    //巡逻模式，上传GPS位置信息
    public static final String mPatrolLocationAdd = "/api/police/v0.1/patrol/location/add";
    //核查任务，上传GPS位置信息
    public static final String mPopulationLocationAdd = "/api/police/v0.1/verification/location/add";
    //警单模式，更改警单的状态
    public static final String mModifyStateUrl = "/api/police/v0.1/record/changeCopTask";
    //巡逻模式，修改巡逻状态
    public static final String mModifyPatrolStateUrl = "/api/police/v0.1/patrol/changeStatus";
    //警单模式，上传视频记录
    private static final String mAddVideoUrl = "/api/police/v0.1/live/addVideo";
    //获取hileia的token信息
    private static final String mHileiaTokenUrl = "/api/police/v0.1/token/getToken";
    //上传Hileia的日志文件
    private static final String mUploadLogUrl = "/api/police/v0.1/test/addFileLog";
    private static final String mNotifyPushStateUrl = "/api/police/v0" +
            ".1/device/hkDeviceChangeStatus";
    private static final String mGetHkDeviceInfoUrl = "/api/police/v0.1/device/hkDeviceInfo";

    //警单模式， 获取视频上传列表和上传状态
    private static final String mVideoListUrl = "/api/police/v0.1/live/videoList";
    //警单状态，获取人脸识别记录列表
    public static final String mFaceListUrl = "/api/police/v0.1/live/faceList";
    //上传用户信息到服务器
    private static final String mAddUserInfoUrl = "/api/police/v0.1/live/addAppUserInfo";
    //警单模式，上传车牌识别结果，和车辆图片
    public static final String mAddCarRecogUrl = "/api/police/v0.1/live/addCarReco";
    //警单模式，获取车辆识别列表信息
    public static final String mCarListUrl = "/api/police/v0.1/live/carList";
    public static final String mPatrolCarListUrl = "/api/police/v0.1/patrol/car/search";
    private static final String mPatrolTaskCreateUrl = "/api/police/v0.1/patrol/create";
    private static final String mPatrolUpdateStatusUrl = "/api/police/v0.1/patrol/changeStatus";
    private static final String mPatrolListSearchUrl = "/api/police/v0.1/patrol/search";

    //查询警单记录的个数，未出警，出警中等的个数
    private static final String mPoliceTaskSummary = "/api/police/v0.1/record/censusCount";

    //查询巡逻记录的个数，出警中，已完成等的个数
    private static final String mPatrolTaskSummary = "/api/police/v0.1/patrol/count";

    //巡逻模式车辆信息添加
    public static final String mPatrolCarAdd = "/api/police/v0.1/patrol/car/add";

    //巡逻模式添加视频记录
    private static final String mPatrolVideoUploadUrl = "/api/police/v0.1/patrol/video/add";
    //巡逻模式查询视频记录
    private static final String mPatrolVideoSearchUrl = "/api/police/v0.1/patrol/video/search";

    //巡逻人脸分组显示信息
    public static final String mPatroFacelistSearchUrl = "/api/police/v0" +
            ".1/patrol/face/searchGroup";

    //人员历史信息查询URL
    public static final String mFaceGroupList = "/api/police/v0.1/live/faceGroupList";

    //车辆信息URL
    private static final String mCarSearchUrl = "/api/police/v0.1/car/search";

    //图片上传Url
    public static final String mPhotoUploadUrl = "/api/police/v0.1/live/addSceneImage";
    //巡逻图片上传URL
    public static final String mPatrolPhotoUploadUrl = "/api/police/v0.1/patrol/sceneImage/add";

    //查询图片状态Url
    private static final String mPhotoSceneListUrl = "/api/police/v0.1/live/sceneImageList";
    //查询图片状态Url
    private static final String mPatrolPhotoListUrl = "/api/police/v0.1/patrol/sceneImage/search";
    //巡逻最新人脸记录查询
    public static final String mPatrolFaceSearchUrl = "/api/police/v0.1/patrol/face/search";

    //出警模式，音频文件添加
    public static final String mAddAudioUrl = "/api/police/v0.1/live/addAudio";
    //音频文件记录查询
    private static final String mAudioListUrl = "/api/police/v0.1/live/audioList";

    //巡逻模式，音频文件添加
    public static final String mAddPatrolAudioUrl = "/api/police/v0.1/patrol/audio/add";
    //巡逻模式，音频文件记录查询
    private static final String mAudioPatrolListUrl = "/api/police/v0.1/patrol/audio/search";

    //查询是否有新的警单
    private static final String mSyncCopTaskUrl = "/api/police/v0.1/live/syncCopTask";

    //查询最近的警单
    private static final String mRecentCopList = "/api/police/v0.1/record/getCjdbh2List";

    //查询最近的巡逻单
    private static final String mRecentPatrolList = "/api/police/v0.1/patrol/getPatroNumList";

    private static final String mLogUrl = "/api/police/v0.1/test/addLog";
    //    private static final String mAppDataDir = "/storage/emulated/0/jwt/com.hiar.ar110/";

    //创建群聊群组
    private static final String mCreateGroup = "/api/police/v0.1/group/create";

    //获取群组成员
    private static final String mGroupmembers = "/api/police/v0.1/group/searchMember";

    //出警证据文件保存根目录
    private static final String mAppDataDir = "/storage/emulated/0/.jwt/com.hiar.ar110/";
    public static final String APP_ID_JWT = "8F260DB012F705FBC86E862B31325C8D";
    public static final String APP_REG_ID = "330000102048";

    //处警状态
    public static final int CJZT_WCJ = 1;    //待接收
    public static final int CJZT_CJZ = 2;    //已接收
    public static final int CJZT_YWC = 3;    //已出警
    /*public static final int CJZT_DDXC = 4;   //已到达现场
    public static final int CJZT_CJWB = 5;   //处警完毕
    public static final int CJZT_CJYFK = 6;  //处警已反馈
    public static final int CJZT_WC = 7;     //处警结束
*/
    public static final int VIDEO_UPLOAD_OK = 2;
    public static final int VIDEO_IS_UPLOADING = 1;
    public static final int VIDEO_NOT_UPLOADED = 0;

    public static final float LIGHT_IN_POCKET = 20.0f;

    public static String KEY_THRESH = "key_face_threshood";
    public static String KEY_LPR_THRESH = "key_lpr_threshood";
    public static String KEY_CAR_RECOG = "key_need_carrecog";
    public static final String KEY_VIDEO_CACHE_TYPE = "key_video_cache_type";
    public static final String KEY_LAST_CLEAN_CACHE_FILE_TIME = "key_last_clean_cache_file_time";
    /**
     * 模块化控制数据
     */
    public static String KEY_MODULE_CONFIG = "key_module_config";

    public static String mNewMsgCjdbh2 = null;
    public static final String FTP_MODE_LOCALPASSIVE = "localPassive";
    public static final String FTP_MODE_REMOTEPASSIVE = "remotePassive";
    public static final String FTP_MODE_LOCALACTIVE = "localActive";
    public static final String FTP_MODE_REMOTEACTIVE = "remoteActive";

    public static String mFtpMode = FTP_MODE_LOCALPASSIVE;
    public static final String mPoliceHeadLibUrl = "http://20.65.2.12:4117";

    public static void initWebConfig() {
        compileVERSION = CompileVersion.valueOf(BuildConfig.COMPILE);
        if (compileVERSION == CompileVersion.POLICE_PRODUCTION) {
            mISExternalWeb = false;
            mBaseIpPolice = "http://20.65.2.12:4136";
            HILEIA_PRIVATE_PORT = "4156";
        } else if (compileVERSION == CompileVersion.POLICE_DEVELOP) {
            mISExternalWeb = false;
            mBaseIpPolice = "http://20.65.2.12:4130";
            HILEIA_PRIVATE_PORT = "4124";
        } else if (compileVERSION == CompileVersion.HISCENE_DEVELOP) {
            mISExternalWeb = false;
            mDebugBaseIp = "112.65.179.30";
            mBaseIp = "http://" + mDebugBaseIp + ":30174";
            //Hileia测试环境IP，端口
            HILEIA_PRIVATE_DEBUG_URL = "112.65.179.30";
            HILEIA_PRIVATE_DEBUG_PORT = "30177";

        } else if (compileVERSION == CompileVersion.HISCENE_EXTERNAL) {
            mISExternalWeb = true;
            mExternal_URL = "http://106.75.214.60:10177";
            mExternalHileiaUrl = "106.75.214.60";
            mExternalHileiaPort = "30177";
            mMqttHost = "tcp://106.75.214.60:1883";
        } else if (compileVERSION == CompileVersion.INTERNAL_DEVELOP) {
            mISExternalWeb = false;
            mDebugBaseIp = "192.168.23.41";
            mBaseIp = "http://" + mDebugBaseIp + ":30180";
            HILEIA_PRIVATE_DEBUG_URL = "192.168.23.41";
            HILEIA_PRIVATE_DEBUG_PORT = "30177";
            mMqttHost = "tcp://192.168.23.65:31885";
        } else if (compileVERSION == CompileVersion.DEBUG_INTERNAL_NEW) {
            mISExternalWeb = false;
            mDebugBaseIp = "192.168.20.13";
            mBaseIp = "http://" + mDebugBaseIp + ":30180";
            HILEIA_PRIVATE_DEBUG_URL = "192.168.20.13";
            HILEIA_PRIVATE_DEBUG_PORT = "30177";
            mMqttHost = "tcp://192.168.23.65:31883";
        } else if (compileVERSION == CompileVersion.ZHAOTONG) {
            mISExternalWeb = false;
            //mExternal_URL = "http://106.75.214.60:10177";
            mDebugBaseIp = "218.63.110.31";
            mBaseIp = "http://" + mDebugBaseIp + ":30180";
            HILEIA_PRIVATE_DEBUG_URL = "218.63.110.31";
            HILEIA_PRIVATE_DEBUG_PORT = "30177";
            mMqttHost = "tcp://218.63.110.31:1883";
        }
        if (Util.mISExternalWeb && BuildConfig.DEBUG) {
            Util.mBaseIp = Util.mExternal_URL;
        }
    }


    public static String getSyncCopTaskUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mSyncCopTaskUrl);
        }

        return (mBaseIpPolice + mSyncCopTaskUrl);
    }

    public static String getAudioAddUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mAddAudioUrl);
        }

        return (mBaseIpPolice + mAddAudioUrl);
    }


    public static String getCopTaskNumberUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mPoliceTaskSummary);
        }

        return (mBaseIpPolice + mPoliceTaskSummary);
    }

    public static String getPatrolTaskNumberUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mPatrolTaskSummary);
        }

        return (mBaseIpPolice + mPatrolTaskSummary);
    }


    public static String getAudioListUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mAudioListUrl);
        }

        return (mBaseIpPolice + mAudioListUrl);
    }

    public static String getPatrolAudioListUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mAudioPatrolListUrl);
        }

        return (mBaseIpPolice + mAudioPatrolListUrl);
    }

    public static String getPatrolAddAudioUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mAddPatrolAudioUrl);
        }

        return (mBaseIpPolice + mAddPatrolAudioUrl);
    }

    public static String getModifyPatrolStateUrlUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mModifyPatrolStateUrl);
        }

        return (mBaseIpPolice + mModifyPatrolStateUrl);
    }

    public static String getPatrolTaskCreateUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mPatrolTaskCreateUrl);
        }

        return (mBaseIpPolice + mPatrolTaskCreateUrl);
    }

    public static String getPatrolAddCarUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mPatrolCarAdd);
        }

        return (mBaseIpPolice + mPatrolCarAdd);
    }

    public static String getPatrolVideoUploadUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mPatrolVideoUploadUrl);
        }

        return (mBaseIpPolice + mPatrolVideoUploadUrl);
    }

    public static String getPatrolVideoSearchUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mPatrolVideoSearchUrl);
        }

        return (mBaseIpPolice + mPatrolVideoSearchUrl);
    }


    public static String getPatrolUpdateStatusUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mPatrolUpdateStatusUrl);
        }

        return (mBaseIpPolice + mPatrolUpdateStatusUrl);
    }


    public static String getPatrolListSearchUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mPatrolListSearchUrl);
        }

        return (mBaseIpPolice + mPatrolListSearchUrl);
    }

    public static String getCarPhotoIpHead() {
        if (BuildConfig.DEBUG) {
            if (mISExternalWeb) {
                return getIpHead() + "/";
            } else {
                return mCarPhotoUrlHead;
            }
        } else {
            //return getIpHead() + "/";
            return getIpHead() + "/";
        }
    }

    public static String getPeoplePhotoIpHead() {
        if (BuildConfig.DEBUG) {
            if (mISExternalWeb) {
                return getIpHead() + "/";
            } else {
                return mFacePhotoUrlHead;
            }
        } else {
            return getIpHead() + "/";
        }
    }


    public static String getCarListUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mCarListUrl);
        }

        return (mBaseIpPolice + mCarListUrl);
    }

    public static String getPatrolCarListUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mPatrolCarListUrl);
        }

        return (mBaseIpPolice + mPatrolCarListUrl);
    }

    public static String getFaceGroupUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mFaceGroupList);
        }

        return (mBaseIpPolice + mFaceGroupList);
    }

    public static String getCarSearchUrl() {
        if (BuildConfig.DEBUG) {
            return "http://" + mDebugBaseIp + ":8890" + mCarSearchUrl;
        } else {
            return mBaseIpPolice + mCarSearchUrl;
        }
    }

    public static String getIpHead() {
        if (mISExternalWeb) {
            return mExternal_URL;
        } else {
            if (BuildConfig.DEBUG) {
                return mBaseIp;
            } else {
                return mBaseIpPolice + "/zj/3302";
            }
        }
    }

    public static String getAddCarRecogUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mAddCarRecogUrl);
        }

        return (mBaseIpPolice + mAddCarRecogUrl);
    }


    public static String getPatrolFaceSearchUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mPatrolFaceSearchUrl);
        }

        return (mBaseIpPolice + mPatrolFaceSearchUrl);
    }

    public static String getFaceListUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mFaceListUrl);
        }

        return (mBaseIpPolice + mFaceListUrl);
    }

    public static String getPatrolFaceListUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mPatroFacelistSearchUrl);
        }

        return (mBaseIpPolice + mPatroFacelistSearchUrl);
    }

    public static String getVideoListUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mVideoListUrl);
        }

        return (mBaseIpPolice + mVideoListUrl);
    }

    public static String getUploadLogUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mUploadLogUrl);
        }

        return (mBaseIpPolice + mUploadLogUrl);
    }


    public static String getRecentCopListUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mRecentCopList);
        }
        return (mBaseIpPolice + mRecentCopList);
    }

    public static String getRecentPatrolListUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mRecentPatrolList);
        }
        return (mBaseIpPolice + mRecentPatrolList);
    }

    public static String getHileiaPrivateUrl() {
        if (BuildConfig.DEBUG) {
            return HILEIA_PRIVATE_DEBUG_URL;
        } else {
            return HILEIA_PRIVATE_URL;
        }
    }

    public static String getHileiaPrivatePort() {
        if (BuildConfig.DEBUG) {
            return HILEIA_PRIVATE_DEBUG_PORT;
        } else {
            return HILEIA_PRIVATE_PORT;
        }
    }

    public static String getPhotoUploadUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mPhotoUploadUrl);
        }

        return (mBaseIpPolice + mPhotoUploadUrl);
    }

    public static String getPatrolPhotoUploadUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mPatrolPhotoUploadUrl);
        }

        return (mBaseIpPolice + mPatrolPhotoUploadUrl);
    }

    public static String getPhotoSceneListUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mPhotoSceneListUrl);
        }

        return (mBaseIpPolice + mPhotoSceneListUrl);
    }

    public static String getPatrolPhotoListUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mPatrolPhotoListUrl);
        }

        return (mBaseIpPolice + mPatrolPhotoListUrl);
    }

    //------------------------------------  sp  ------------------------------------
    public static String getStringPref(Context context, String name, String def) {
        SharedPreferences prefs = context.getSharedPreferences(
                context.getPackageName(), Context.MODE_PRIVATE);
        String ret = prefs.getString(name, def);
        return ret;
    }

    public static void setStringPref(Context context, String name, String val) {
        SharedPreferences prefs = context.getSharedPreferences(
                context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = prefs.edit();
        ed.putString(name, val);
        SharedPreferencesCompat.apply(ed);
    }

    public static float getFloatPref(Context context, String name, float def) {
        SharedPreferences prefs = context.getSharedPreferences(
                context.getPackageName(), Context.MODE_PRIVATE);
        float ret = prefs.getFloat(name, def);
        return ret;
    }

    public static void setFloatPref(Context context, String name, float def) {
        SharedPreferences prefs = context.getSharedPreferences(
                context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = prefs.edit();
        ed.putFloat(name, def);
        SharedPreferencesCompat.apply(ed);
    }

    public static int getIntPref(Context context, String name, int def) {
        SharedPreferences prefs = context.getSharedPreferences(
                context.getPackageName(), Context.MODE_PRIVATE);
        int ret = prefs.getInt(name, def);
        return ret;
    }

    public static void setIntPref(Context context, String name, int def) {
        SharedPreferences prefs = context.getSharedPreferences(
                context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = prefs.edit();
        ed.putInt(name, def);
        SharedPreferencesCompat.apply(ed);
    }

    public static long getLongPref(Context context, String name, long def) {
        SharedPreferences prefs = context.getSharedPreferences(
                context.getPackageName(), Context.MODE_PRIVATE);
        long ret = prefs.getLong(name, def);
        return ret;
    }

    public static void setLongPref(Context context, String name, long def) {
        SharedPreferences prefs = context.getSharedPreferences(
                context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = prefs.edit();
        ed.putLong(name, def);
        SharedPreferencesCompat.apply(ed);
    }

    public static void initBaseip(Context context) {
        if (BuildConfig.DEBUG) {
            mBaseIp = getStringPref(context, "key_base_ip", mBaseIp);
        } else {
            mBaseIpPolice = getStringPref(context, "key_base_ip", mBaseIpPolice);
        }
    }

    public static String getAddUserInfoUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mAddUserInfoUrl);
        }

        return (mBaseIpPolice + mAddUserInfoUrl);
    }

    public static String getHileiaTokenUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mHileiaTokenUrl);
        }

        return (mBaseIpPolice + mHileiaTokenUrl);
    }

    public static String getCopTaskRecordUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mCopTaskRecord);
        }

        return (mBaseIpPolice + mCopTaskRecord);
    }

    public static String createGroupUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mCreateGroup);
        }

        return (mBaseIpPolice + mCreateGroup);
    }

    public static String getGroupMembersUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mGroupmembers);
        }

        return (mBaseIpPolice + mGroupmembers);
    }


    public static String getModifyStateUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mModifyStateUrl);
        }

        return (mBaseIpPolice + mModifyStateUrl);
    }

    public static String getLogUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mLogUrl);
        }

        return (mBaseIpPolice + mLogUrl);
    }

    public static String getAddVideoUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mAddVideoUrl);
        }

        return (mBaseIpPolice + mAddVideoUrl);
    }

    public static void setBaseIp(Context context, String ip) {
        setStringPref(context, "key_base_ip", ip);
        if (BuildConfig.DEBUG) {
            mBaseIp = ip;
        } else {
            mBaseIpPolice = ip;
        }
    }

    public static String getAddFaceUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mFaceUploadUrl);
        }

        return (mBaseIpPolice + mFaceUploadUrl);
    }

    public static String getPatrolAddFaceUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mPatrolAddFace);
        }

        return (mBaseIpPolice + mPatrolAddFace);
    }

    public static String getNotifyPushStateUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mNotifyPushStateUrl);
        }

        return (mBaseIpPolice + mNotifyPushStateUrl);
    }

    public static String getDeviceInfoUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mGetHkDeviceInfoUrl);
        }

        return (mBaseIpPolice + mGetHkDeviceInfoUrl);
    }

    public static String getLocationUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mLocationUrl);
        }

        return (mBaseIpPolice + mLocationUrl);
    }

    public static String getPatrolLocationUrl() {
        if (BuildConfig.DEBUG) {
            return (mBaseIp + mPatrolLocationAdd);
        }

        return (mBaseIpPolice + mPatrolLocationAdd);
    }


    public static String getProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, key, defaultValue));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return value;
        }
    }

    public static String getAppDataPath(String cjdh) {
        File folder = new File(mAppDataDir + cjdh);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return (mAppDataDir + cjdh);
    }

    public static String getVideoSavePath(String cjdh) {
        File folder = new File(mAppDataDir + cjdh);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String path = mAppDataDir + cjdh + "/" + getRecordFileName() + ".mp4";
        return path;
    }

    public static String getRecordFileName() {
        return formatDate();
    }

    public static String formatDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        return format.format(new Date());
    }

    public static String formatDatePatrol() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date());
    }


    /**
     * 计算Sdcard的剩余大小
     *
     * @return MB
     */
    public static long getAvailableSize() {
        String state = Environment.MEDIA_MOUNTED;
        String sdcard = Environment.getExternalStorageState();
        File file = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(file.getPath());
        if (sdcard.equals(state)) {
            //获得Sdcard上每个block的size
            long blockSize = statFs.getBlockSize();
            //获取可供程序使用的Block数量
            long blockavailable = statFs.getAvailableBlocks();
            //计算标准大小使用：1024，当然使用1000也可以
            long blockavailableTotal = blockSize * blockavailable / 1024 / 1024;
            return blockavailableTotal;
        } else {
            return -1;
        }
    }

    private static Toast mToast;

    public static void showMessage1(String msg) {
        if (mToast != null) {
            mToast.setText(msg);
            mToast.setDuration(Toast.LENGTH_SHORT);
            mToast.show();
        } else {
            mToast = Toast.makeText(Utils.getApp().getApplicationContext(), msg, Toast.LENGTH_LONG);
            mToast.show();
        }
    }

    public static void showMessage(String msg) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            ThreadUtils.runOnUiThread(() -> {
                ToastUtils.show(msg);
            });
        } else {
            ToastUtils.show(msg);
        }
    }

    public static String getIMEI(int slotId) {
        try {
            Class clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getMethod("get", String.class, String.class);
            String imei = (String) method.invoke(null, "ril.gsm.imei", "");
            if (!TextUtils.isEmpty(imei)) {
                String[] split = imei.split(",");
                if (split.length > slotId) {
                    imei = split[slotId];
                }
                return imei;
            } else {
                imei = Settings.System.getString(
                        Utils.getApp().getContentResolver(), Settings.Secure.ANDROID_ID);
            }
            return imei;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            e.printStackTrace();
            Log.w("imei", "get android id error : " + e.getMessage());
        }
        return "";
    }

    public static String getAndroidID() {
        return Settings.System.getString(
                Utils.getApp().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getMEID() {
        try {
            Class clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getMethod("get", String.class, String.class);

            String meid = (String) method.invoke(null, "ril.cdma.meid", "");
            if (!TextUtils.isEmpty(meid)) {
                AR110Log.i(TAG, "getMEID meid: " + meid);
                return meid;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            e.printStackTrace();
            Log.w(TAG, "getMEID error : " + e.getMessage());
        }
        return "";
    }

    public static String getIMSI(Context context) {
        try {
            TelephonyManager telephonyManager =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            //获取IMSI号
            @SuppressLint("MissingPermission") String imsi = telephonyManager.getSubscriberId();
            if (null == imsi) {
                imsi = Settings.System.getString(
                        Utils.getApp().getContentResolver(), Settings.Secure.ANDROID_ID);
            }
            return imsi;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getIpAddressString() {
        try {
            for (Enumeration<NetworkInterface> enNetI = NetworkInterface.getNetworkInterfaces(); enNetI.hasMoreElements(); ) {
                NetworkInterface netI = enNetI.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = netI
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "0.0.0.0";

    }

//    public static void sendLogToServer(final String info) {
//        RequestParams params = new RequestParams(getLogUrl());
//        params.addBodyParameter("commit", info);
//        x.http().post(params, new CommonCallbackImpl<String>());
//    }

    public static void ARLOG_JDLIST(String UTAG, String log) {
        if (BuildConfig.DEBUG) {
            Log.d(UTAG, log);
            //AR110Log.d(UTAG, log);
        } else {
            //sendLogToServer(UTAG + ":::" + log);
            //AR110Log.d(UTAG, log);
        }
    }

    public static void ARLOG_PATROLLIST(String UTAG, String log) {
        if (BuildConfig.DEBUG) {
            Log.d(UTAG, log);
            //AR110Log.d(UTAG, log);
        } else {
            //sendLogToServer(UTAG + ":::" + log);
            //AR110Log.d(UTAG, log);
        }
    }

    public static void ARLOG_GPS(String UTAG, String log) {
        if (BuildConfig.DEBUG) {
            Log.d(UTAG, log);
            //AR110Log.d(UTAG, log);
        } else {
            //sendLogToServer(UTAG + ":::" + log);
            //AR110Log.d(UTAG, log);
        }
    }


    public static void ARLOG(String UTAG, String log) {
        if (BuildConfig.DEBUG) {
            Log.d(UTAG, log);
            //AR110Log.d(UTAG, log);
        } else {
            //sendLogToServer(UTAG + ":::" + log);
            //AR110Log.d(UTAG, log);
        }
    }

    public static void ARLOG_CAR(String UTAG, String log) {
        if (BuildConfig.DEBUG) {
            //Log.d(UTAG,log);
            //AR110Log.d(UTAG, log);
        } else {
            //sendLogToServer(UTAG + ":::" + log);
            //AR110Log.d(UTAG, log);
        }
    }

    public static void ARLOG_FACE(String UTAG, String log) {
        if (BuildConfig.DEBUG) {
            //Log.d(UTAG,log);
            //AR110Log.d(UTAG, log);
        } else {
            //sendLogToServer(UTAG + ":::" + log);
            //AR110Log.d(UTAG, log);
        }
    }

    /**
     * 获取apk的版本号 currentVersionCode
     *
     * @param ctx
     * @return
     */
    public static String getAPPVersionName(Context ctx) {
        String appVersionName = null;
        PackageManager manager = ctx.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
            appVersionName = info.versionName; // 版本名
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appVersionName;
    }

    /**
     * get App versionCode
     *
     * @param context
     * @return
     */
    public static String getVersionCode(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        String versionCode = "";
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode + "";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }


    private static class StringConverter implements JsonSerializer<String>,
            JsonDeserializer<String> {
        public JsonElement serialize(String src, Type typeOfSrc,
                                     JsonSerializationContext context) {
            if (src == null) {
                return new JsonPrimitive("");
            } else {
                return new JsonPrimitive(src.toString());
            }
        }

        public String deserialize(JsonElement json, Type typeOfT,
                                  JsonDeserializationContext context)
                throws JsonParseException {
            return json.getAsJsonPrimitive().getAsString();
        }
    }

    public static Gson getGson() {
        GsonBuilder gb = new GsonBuilder();
        gb.registerTypeAdapter(String.class, new StringConverter());
        Gson gson = gb.create();
        return gson;
    }

    public static File createVideoThumbnail(File videoFile) {
        String filePath = videoFile.getAbsolutePath();
        String thumbnails = filePath.replace(".mp4", ".jpg");
        File thumbFile = new File(thumbnails);
        if (thumbFile.exists()) {
            if (thumbFile.length() > 10000) {
                return thumbFile;
            }
        } else {
            try {
                thumbFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(-1);
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }

        if (bitmap == null) {
            return null;
        }

        // Scale down the bitmap if it's too large.
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int max = Math.max(width, height);
        if (max > 512) {
            float scale = 512f / max;
            int w = Math.round(scale * width);
            int h = Math.round(scale * height);
            bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
        }

        try {
            FileOutputStream os = new FileOutputStream(thumbnails);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return thumbFile;
    }

    public static boolean isLocationEnabled() {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(Utils.getApp().getContentResolver(),
                        Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(Utils.getApp().getContentResolver(),
                    Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static UserInfoData getUserInfo(Context context) {
        if (BuildConfig.DEBUG) {
            String androidId = Util.getAndroidID();
            String sufix = androidId.substring(androidId.length() - 4);
            String name = Util.getStringPref(Utils.getApp(), LoginConstants.LOGIN_NAME, "");
            ModuleConfig.INSTANCE.setLoginName(name);
            UserInfoData mUserInfoData = new UserInfoData();
            mUserInfoData.account = "ar110";
            mUserInfoData.avatarUrl = "unknown";
            mUserInfoData.deptId = "firmware dep";
            mUserInfoData.email = "hiscene@hiscene.com";
            mUserInfoData.idcard = "331404199809081129";
            mUserInfoData.name = "执勤人员" + ModuleConfig.INSTANCE.getLoginName();
            mUserInfoData.phone = "13897654431";
            mUserInfoData.position = "执勤人员";
            mUserInfoData.sex = "男";
            mUserInfoData.uuid = "uuid";
            return mUserInfoData;
        } else {
            try {
                User user = UserInfo.getUser(context);
                if (user != null) {
                    UserInfoData mUserInfoData = new UserInfoData(user);
                    return mUserInfoData;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * @param uploadList
     * @param fileName
     * @param needCheckIsUpload 是否需要判断同步到省厅 警单需要，其他的不需要
     * @return 返回值大于0，表示传到了FTP服务器，没有同步到省厅
     * -1,表示上传到了省厅
     * -2 表示从来没有上传过
     */
    public static int isFileUploaded(List<? extends FileUploadData> uploadList,
                                     String fileName, boolean needCheckIsUpload) {
        int len = uploadList.size();
        int i = 0;
        for (i = 0; i < len; i++) {
            FileUploadData item = uploadList.get(i);
            if (needCheckIsUpload) {
                if (item.mUrl.equals(fileName) && item.isUpload == 0) {
                    return i;
                }
                if (item.mUrl.equals(fileName) && item.isUpload == 1) {
                    return -1;
                }
            } else {
                if (item.mUrl.equals(fileName)) {
                    return -1;
                }
            }
        }

        if (i == len) {
            return -2;
        }

        return -1;
    }

    public static int isFileUploaded(ArrayList<VideoUploadState> uploadList, String fileName) {
        return isFileUploaded(uploadList, fileName, true);
    }

    public static int isPhotoUploaded(ArrayList<PhotoUploadStatus> uploadList, String fileName) {
        return isFileUploaded(uploadList, fileName, true);
    }

    //返回值大于0，表示传到了FTP服务器，没有同步到省厅
    //-1,表示上传到了省厅
    //-2 表示从来没有上传过
    public static int isAudioUploaded(ArrayList<AudioUploadStatus> uploadList, String fileName) {
        return isFileUploaded(uploadList, fileName, true);
    }

    public static boolean isDigit(String strNum) {
        return strNum.matches("[0-9]{1,}");
    }

    public static String getPhotoRootDir(String cjdbh) {
        String path = getJDFileRootDir() + cjdbh + "/photo/";
        File taskFolder = new File(path);
        if (!taskFolder.exists()) {
            taskFolder.mkdirs();
        }
        return path;
    }

    public static String getVideoRootDir(String cjdbh) {
        String path = getJDFileRootDir() + cjdbh + "/video/";
        File taskFolder = new File(path);
        if (!taskFolder.exists()) {
            taskFolder.mkdirs();
        }
        return path;
    }


    public static String getAudioRootDir(String cjdbh) {
        String path = getJDFileRootDir() + cjdbh + "/audio/";
        File taskFolder = new File(path);
        if (!taskFolder.exists()) {
            taskFolder.mkdirs();
        }
        return path;
    }

    public static String getJDFileRootDir() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.jwt/com" +
                ".hiar.ar110/";
        File taskFolder = new File(path);
        if (!taskFolder.exists()) {
            taskFolder.mkdirs();
        }
        return path;
    }

    /**
     * 获取音频文件的总时长大小
     *
     * @param filePath 音频文件路径
     * @return 返回时长大小
     */
    public static long getAudioFileVoiceTime(String filePath) {
        long mediaPlayerDuration = 0L;
        if (filePath == null || filePath.isEmpty()) {
            return 0;
        }
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayerDuration = mediaPlayer.getDuration();
        } catch (IOException ioException) {

        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }
        return mediaPlayerDuration;
    }


    // 方法3：将字符串转化为字节数组，每个汉字占2个字节（GBK），编码格式不同汉字所占的字节数也不同。（例如：utf-8中汉字占3个字节）
    // 并且汉字转换成字节均为负数，可以使用注释掉的代码进行测试
    // 使用此种方法需要注意，中文标点所占字节数与汉字相同
    public static int findChinessNum(String content) {
        byte[] bytes = content.getBytes();
        int num = 0;
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] < 0) {
                num++;
                i = i + 1;
            }
            // System.out.println(bytes[i]);
        }

        return num;
    }

    /**
     * 判断悬浮窗权限
     */
    public static boolean ifops(Context context) {
        boolean canDraw = false;
        if (Build.VERSION.SDK_INT >= 23) {
            canDraw = Settings.canDrawOverlays(context);
            return canDraw;
        } else if (Build.VERSION.SDK_INT < 19) {
            canDraw = true;
        }
        if (!canDraw && Build.VERSION.SDK_INT >= 19) {
            AppOpsManager appOpsMgr =
                    (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            if (appOpsMgr == null) {
                return true;
            } else {
                try {
                    Class cls = Class.forName("android.content.Context");
                    Field declaredField = cls.getDeclaredField("APP_OPS_SERVICE");
                    declaredField.setAccessible(true);
                    Object obj = declaredField.get(cls);
                    if (!(obj instanceof String)) {
                        return false;
                    }
                    String str2 = (String) obj;
                    obj = cls.getMethod("getSystemService", String.class).invoke(context, str2);
                    cls = Class.forName("android.app.AppOpsManager");
                    Field declaredField2 = cls.getDeclaredField("MODE_ALLOWED");
                    declaredField2.setAccessible(true);
                    Method checkOp = cls.getMethod("checkOp", Integer.TYPE, Integer.TYPE,
                            String.class);
                    int result = (Integer) checkOp.invoke(obj, 24, Binder.getCallingUid(),
                            context.getPackageName());
                    return result == declaredField2.getInt(cls);
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return canDraw;
    }
}
