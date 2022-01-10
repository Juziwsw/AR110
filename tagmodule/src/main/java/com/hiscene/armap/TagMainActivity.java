package com.hiscene.armap;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hiscene.gis.Point2d;
import com.hiscene.gis.PointLonLat;
import com.hiscene.gis.Position;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class TagMainActivity extends Activity {
    public static String TAG = "ARMap";
    private CameraView cameraView = null;
    private CoordConverter.CameraPTZ mCameraPTZ = null;
    private PointLonLat mCameraPos = null;
    private Map<TextView, TagInfo> mTags = new HashMap<>();
    private ViewGroup mTagLayout = null;
    private Button mButton = null;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            // 要做的事情
            switch (msg.what) {
                case 0:
                    updateTags();
                    mHandler.sendEmptyMessageDelayed(0, 100);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tag);
        requestPermissions();
        init();
        mTagLayout = findViewById(R.id.tag_layout);
        cameraView = findViewById(R.id.camera);
        mButton = findViewById(R.id.button);
        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                //cameraKitImage.getJpeg();
                //cameraKitImage.getBitmap();

            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {
                //cameraKitImage.getVideoFile();
            }
        });

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cameraView.isStarted()){
                    cameraView.stop();
                }else{
                    cameraView.start();
                }
            }
        });


        //camera
        mCameraPTZ = CoordConverter.getInstance().new CameraPTZ(0, 0, 1);
        mCameraPos = new PointLonLat(121.628633, 31.208547, 0);

        //tags
        TextView view;
        String txt;

        txt = "天之骄子";
        view = new TextView(this);
        view.setText(txt);
        view.setBackgroundColor(Color.rgb(156, 47, 97));
        mTagLayout.addView(view);
        mTags.put(view, new TagInfo(txt, new Point2d(0.5, 0.5), new PointLonLat(121.628441, 31.212758, 0)));


        txt = "集贤桥天主堂";
        view = new TextView(this);
        view.setText(txt);
        view.setBackgroundColor(Color.rgb(34, 190, 97));
        mTagLayout.addView(view);
        mTags.put(view, new TagInfo(txt, new Point2d(0.5, 0.5), new PointLonLat(121.628806, 31.206555, 0)));


        txt = "广兰路地铁站";
        view = new TextView(this);
        view.setText(txt);
        view.setBackgroundColor(Color.rgb(234, 56, 87));
        mTagLayout.addView(view);
        mTags.put(view, new TagInfo(txt, new Point2d(0.5, 0.5), new PointLonLat(121.621059, 31.211088, 0)));


        txt = "北大微电子100米高无人机";
        view = new TextView(this);
        view.setText(txt);
        view.setBackgroundColor(Color.rgb(156, 147, 84));
        mTagLayout.addView(view);
        mTags.put(view, new TagInfo(txt, new Point2d(0.5, 0.5), new PointLonLat(121.626449, 31.207895, 80)));


        txt = "叮咚买菜南门口";
        view = new TextView(this);
        view.setText(txt);
        view.setBackgroundColor(Color.rgb(78, 93, 84));
        mTagLayout.addView(view);
        mTags.put(view, new TagInfo(txt, new Point2d(0.5, 0.5), new PointLonLat(121.628295, 31.208716, 0)));


        txt = "园区东大门";
        view = new TextView(this);
        view.setText(txt);
        view.setBackgroundColor(Color.rgb(78, 93, 84));
        mTagLayout.addView(view);
        mTags.put(view, new TagInfo(txt, new Point2d(0.5, 0.5), new PointLonLat(121.62926, 31.207478, 0)));


        txt = "展讯红绿灯路口";
        view = new TextView(this);
        view.setText(txt);
        view.setBackgroundColor(Color.rgb(123, 93, 84));
        mTagLayout.addView(view);
        mTags.put(view, new TagInfo(txt, new Point2d(0.5, 0.5), new PointLonLat(121.627474, 31.211446, 0)));

        txt = "全家";
        view = new TextView(this);
        view.setText(txt);
        view.setBackgroundColor(Color.rgb(23, 145, 124));
        mTagLayout.addView(view);
        mTags.put(view, new TagInfo(txt, new Point2d(0.5, 0.5), new PointLonLat(121.627742, 31.210524, 0)));

        mHandler.sendEmptyMessageDelayed(0, 1000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void requestPermissions() {
        if(ContextCompat.checkSelfPermission(TagMainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            //未开启定位权限,开启定位权限,200是标识码
            ActivityCompat.requestPermissions(TagMainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},200);
        }else{
            Toast.makeText(TagMainActivity.this,"已开启定位权限",Toast.LENGTH_LONG).show();
        }
    }

    private void updateTags() {
        Log.d(TAG, "-------------------------------------");
        float[] oris = PoseManager.getInstance().getOrientations();
        double[] lonlat = LocationManager.getInstance().getLonlat();

        Log.d(TAG, "getOrientations " + Arrays.toString(oris));
        Log.d(TAG, "getLonlat " + Arrays.toString(lonlat));

        mCameraPTZ.pan = oris[0];
        mCameraPTZ.tilt = oris[1];

        mCameraPos.lon = lonlat[0];
        mCameraPos.lat = lonlat[1];
        mCameraPos.height = lonlat[2];

        int w = mTagLayout.getWidth();
        int h = mTagLayout.getHeight();
        for (Map.Entry<TextView, TagInfo> entry : mTags.entrySet()) {
            TextView txt = entry.getKey();
            TagInfo tagInfo = entry.getValue();
            Position tagPos = CoordConverter.getInstance().lonLatPointToScreen(mCameraPTZ, mCameraPos, tagInfo.lonLat);
            int dis = (int) (Math.sqrt(tagPos.cameraPoint.x * tagPos.cameraPoint.x + tagPos.cameraPoint.y * tagPos.cameraPoint.y + tagPos.cameraPoint.z * tagPos.cameraPoint.z));
            tagInfo.setScreenPoint(tagPos.screenPoint);
            int dx = (int) (w * tagInfo.screenPoint.x) - txt.getWidth() / 2;
            int dy = (int) (h * tagInfo.screenPoint.y) - txt.getHeight() / 2;
            txt.setText(tagInfo.name + "[" + dis + "米]");
            //设置位置
            txt.setX(dx);
            txt.setY(dy);
        }
    }

    private void init(){
        PoseManager.getInstance().init(this);
        PoseManager.getInstance().start();

        LocationManager.getInstance().init(this);
        LocationManager.getInstance().start();


        //init CoordConverter
        CoordConverter.CameraIntrinsic intrinsic = CoordConverter.getInstance().new CameraIntrinsic();
        intrinsic.width = 640;
        intrinsic.height = 480;
        intrinsic.fx = 840.633236f;              /// focal length x
        intrinsic.fy = 842.661973f;              /// focal length y
        intrinsic.cx = 304.913086f;              /// principal point x
        intrinsic.cy = 252.313507f;              /// principal point y
        intrinsic.k1 = 0;              /// first radial distortion coefficient
        intrinsic.k2 = 0;              /// second radial distortion coefficient
        intrinsic.k3 = 0;              /// third radial distortion coefficient
        intrinsic.p1 = 0;              /// first tangential distortion coefficient
        intrinsic.p2 = 0;              /// second tangential distortion coefficient
        intrinsic.zoom = 1;            /// scale factor
        CoordConverter.getInstance().setCameraIntrinsics(2, intrinsic);
    }

    @Override
    protected void onResume() {
        cameraView.start();
        super.onResume();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }


}
