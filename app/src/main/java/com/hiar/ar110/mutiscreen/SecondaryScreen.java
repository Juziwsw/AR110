package com.hiar.ar110.mutiscreen;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hiar.ar110.R;
import com.hiar.ar110.data.cop.CopTaskRecord;
import com.hiar.ar110.data.cop.PatrolRecord;
import com.hiar.ar110.service.AR110BaseService;
import com.hiar.ar110.widget.MySurfaceView;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;

public class SecondaryScreen extends Presentation {
    private ConstraintLayout mTagLayout;
    private ImageView mImgFaceLibGlass;
    private ImageView mImgFaceCropGlass;
    private TextView mTextNameGlass;
    private TextView mTextConfGlass;
    private TextView mTextCrimeGlass;
    private TextView mTextIDcardGlass;
    private ImageView mIconRecording;
    private ImageView mIconFaceCompare;
    private TextView mTextBjrGlass;
    private TextView mTextAfddGlass;
    private TextView mTextBjnrGlass;
    private ConstraintLayout mLayoutGlassContent;
    private ConstraintLayout mLayoutGlassPeoResult;
    private ConstraintLayout mLayoutGlassBjd;
    private ConstraintLayout mLayoutGlassCar;
    private ConstraintLayout mLayoutLive;
    private ImageView mImgPicShowGlass;

    private ImageView mImgCarGlass;
    private TextView mTextPlateNumberGlass;
    private TextView mTextCarBrandGlass;
    private TextView mImgCarTypeGlass;
    private TextView mTextCarColorGlass;
    private TextView mTextRecordTimeGlass;

    private TextView mSecondTextLegalname;
    private TextView mSecondTextDriverID;

    private TextView mTextImportName;
    private TextView mTextImportID;

    private TextView mTextFatalPeo;
    private TextView mTextFatalPeoNum;
    private TextView mTextWarningPeo;
    private TextView mTextWarningPeoNum;
    private TextView mTextBjrNote;

    private Group mGroupGlassRecordTime;
    private MySurfaceView mFaceView;
    private boolean is1080p =false;

    private CopTaskRecord mCopTask;
    private PatrolRecord mPatrolTask;
    private ArrayList<View> secondScreenViewList = new ArrayList<>();

    public ArrayList<View> getViewBySecondScreen() {
        return secondScreenViewList;
    }

    public HashMap<Integer, View> getViewMapOfSecondScreen() {
        return mHashMapSecondScreen;
    }

    private HashMap<Integer, View> mHashMapSecondScreen = new HashMap<>();

    public void initView(View contentView, CopTaskRecord mCopTask, PatrolRecord patrolRecord) {
        secondScreenViewList.clear();
        mTagLayout = contentView.findViewById(R.id.layout_tag_show);
        mHashMapSecondScreen.put(R.id.layout_tag_show, mTagLayout);

        mImgFaceLibGlass = contentView.findViewById(R.id.glass_face_lib);
        mHashMapSecondScreen.put(R.id.glass_face_lib, mImgFaceLibGlass);

        mImgFaceCropGlass = contentView.findViewById(R.id.glass_face_crop);
        mHashMapSecondScreen.put(R.id.glass_face_crop, mImgFaceCropGlass);

        mTextNameGlass = contentView.findViewById(R.id.glass_people_name);
        mHashMapSecondScreen.put(R.id.glass_people_name, mTextNameGlass);

        mTextConfGlass = contentView.findViewById(R.id.glass_conf);
        mHashMapSecondScreen.put(R.id.glass_conf, mTextConfGlass);

        mTextCrimeGlass = contentView.findViewById(R.id.glass_crime_type);
        mHashMapSecondScreen.put(R.id.glass_crime_type, mTextCrimeGlass);

        mTextIDcardGlass = contentView.findViewById(R.id.glass_id_name);
        mHashMapSecondScreen.put(R.id.glass_id_name, mTextIDcardGlass);

        mIconRecording = contentView.findViewById(R.id.icon_recording);
        mHashMapSecondScreen.put(R.id.icon_recording, mIconRecording);

        mIconFaceCompare = contentView.findViewById(R.id.icon_face_recog);
        mHashMapSecondScreen.put(R.id.icon_face_recog, mIconFaceCompare);

        mTextBjrGlass = contentView.findViewById(R.id.glass_text_bjr);
        mHashMapSecondScreen.put(R.id.glass_text_bjr, mTextBjrGlass);

        mTextAfddGlass = contentView.findViewById(R.id.glass_text_afdd);
        mHashMapSecondScreen.put(R.id.glass_text_afdd, mTextAfddGlass);

        mTextBjnrGlass = contentView.findViewById(R.id.glass_text_bjnr);
        mHashMapSecondScreen.put(R.id.glass_text_bjnr, mTextBjnrGlass);

        mFaceView = contentView.findViewById(R.id.face_view);
        mHashMapSecondScreen.put(R.id.face_view, mFaceView);
        mFaceView.setmScale(is1080p ?1.5f:1f);

        if (null != mCopTask) {
            mTextBjrGlass.setText(mCopTask.bjrxm);
            mTextAfddGlass.setText(mCopTask.afdd);
            mTextBjnrGlass.setText(mCopTask.bjnr);
        } else if (patrolRecord != null) {
            mTextBjrGlass.setText(AR110BaseService.mUserInfo.name);
        }

        mLayoutGlassContent = contentView.findViewById(R.id.layout_glass_content_show);
        mHashMapSecondScreen.put(R.id.layout_glass_content_show, mLayoutGlassContent);

        mLayoutGlassPeoResult = mLayoutGlassContent.findViewById(R.id.people_result_glass);
        mHashMapSecondScreen.put(R.id.people_result_glass, mLayoutGlassPeoResult);

        mLayoutGlassBjd = mLayoutGlassContent.findViewById(R.id.glass_bjd);
        mHashMapSecondScreen.put(R.id.glass_bjd, mLayoutGlassBjd);

        mLayoutGlassCar = mLayoutGlassContent.findViewById(R.id.layout_car_content);
        mHashMapSecondScreen.put(R.id.layout_car_content, mLayoutGlassCar);

        mImgCarGlass = mLayoutGlassCar.findViewById(R.id.img_car_glass);
        mHashMapSecondScreen.put(R.id.img_car_glass, mImgCarGlass);

        mTextPlateNumberGlass = mLayoutGlassCar.findViewById(R.id.text_platenum_glass);
        mHashMapSecondScreen.put(R.id.text_platenum_glass, mTextPlateNumberGlass);

        mTextCarBrandGlass = mLayoutGlassCar.findViewById(R.id.text_car_brand_glass);
        mHashMapSecondScreen.put(R.id.text_car_brand_glass, mTextCarBrandGlass);

        mImgCarTypeGlass = mLayoutGlassCar.findViewById(R.id.text_car_type_glass);
        mHashMapSecondScreen.put(R.id.text_car_type_glass, mImgCarTypeGlass);

        mTextCarColorGlass = mLayoutGlassCar.findViewById(R.id.text_car_color_glass);
        mHashMapSecondScreen.put(R.id.text_car_color_glass, mTextCarColorGlass);

        mTextRecordTimeGlass = contentView.findViewById(R.id.glass_text_recordtime);
        mHashMapSecondScreen.put(R.id.glass_text_recordtime, mTextRecordTimeGlass);

        mGroupGlassRecordTime = contentView.findViewById(R.id.group_glass_recordtime);
        mHashMapSecondScreen.put(R.id.group_glass_recordtime, mGroupGlassRecordTime);

        mLayoutLive = contentView.findViewById(R.id.layout_zhzx);
        mHashMapSecondScreen.put(R.id.layout_zhzx, mLayoutLive);

        mGroupGlassRecordTime.setVisibility(View.INVISIBLE);
        mImgPicShowGlass = findViewById(R.id.img_pic_show_glass);
        mHashMapSecondScreen.put(R.id.img_pic_show_glass, mImgPicShowGlass);

        mSecondTextLegalname = findViewById(R.id.text_legal_name);
        mHashMapSecondScreen.put(R.id.text_legal_name, mSecondTextLegalname);

        mSecondTextDriverID = findViewById(R.id.text_driver_idcard);
        mHashMapSecondScreen.put(R.id.text_driver_idcard, mSecondTextDriverID);

        mTextImportName = findViewById(R.id.glass_important_peoname);
        mHashMapSecondScreen.put(R.id.glass_important_peoname, mTextImportName);

        mTextImportID = findViewById(R.id.glass_important_peoidcard);
        mHashMapSecondScreen.put(R.id.glass_important_peoidcard, mTextImportID);

        mTextFatalPeo = findViewById(R.id.text_fatal_peo);
        mHashMapSecondScreen.put(R.id.text_fatal_peo, mTextFatalPeo);

        mTextFatalPeoNum = findViewById(R.id.text_fatal_peonum);
        mHashMapSecondScreen.put(R.id.text_fatal_peonum, mTextFatalPeoNum);

        mTextWarningPeo = findViewById(R.id.text_warning_peo);
        mHashMapSecondScreen.put(R.id.text_warning_peo, mTextWarningPeo);

        mTextWarningPeoNum = findViewById(R.id.text_warning_peonum);
        mHashMapSecondScreen.put(R.id.text_warning_peonum, mTextWarningPeoNum);

        mTextBjrNote = findViewById(R.id.glass_text_bjr_note);
        mHashMapSecondScreen.put(R.id.glass_text_bjr_note, mTextBjrNote);

        secondScreenViewList.add(mTagLayout);
        secondScreenViewList.add(mImgFaceLibGlass);
        secondScreenViewList.add(mImgFaceCropGlass);
        secondScreenViewList.add(mTextNameGlass);
        secondScreenViewList.add(mTextConfGlass);
        secondScreenViewList.add(mTextCrimeGlass);
        secondScreenViewList.add(mTextIDcardGlass);
        secondScreenViewList.add(mIconRecording);
        secondScreenViewList.add(mIconFaceCompare);
        secondScreenViewList.add(mTextBjrGlass);
        secondScreenViewList.add(mTextAfddGlass);
        secondScreenViewList.add(mTextBjnrGlass);
        secondScreenViewList.add(mLayoutGlassContent);
        secondScreenViewList.add(mLayoutGlassPeoResult);
        secondScreenViewList.add(mLayoutGlassBjd);
        secondScreenViewList.add(mLayoutGlassCar);
        secondScreenViewList.add(mImgCarGlass);
        secondScreenViewList.add(mTextPlateNumberGlass);
        secondScreenViewList.add(mTextCarBrandGlass);
        secondScreenViewList.add(mImgCarTypeGlass);
        secondScreenViewList.add(mTextCarColorGlass);
        secondScreenViewList.add(mTextRecordTimeGlass);
        secondScreenViewList.add(mGroupGlassRecordTime);
        secondScreenViewList.add(mFaceView);
        secondScreenViewList.add(mLayoutLive);
        secondScreenViewList.add(mImgPicShowGlass);
        secondScreenViewList.add(mSecondTextLegalname);
        secondScreenViewList.add(mSecondTextDriverID);
        secondScreenViewList.add(mTextImportName);
        secondScreenViewList.add(mTextImportID);
        secondScreenViewList.add(mTextFatalPeo);
        secondScreenViewList.add(mTextFatalPeoNum);
        secondScreenViewList.add(mTextWarningPeo);
        secondScreenViewList.add(mTextWarningPeoNum);
        secondScreenViewList.add(mTextBjrNote);
    }

    public SecondaryScreen(Context outerContext, Display display, CopTaskRecord task, PatrolRecord patrolRecord) {
        super(outerContext, display);
        mCopTask = task;
        mPatrolTask = patrolRecord;
    }

    public void setTask(CopTaskRecord task, PatrolRecord patrolRecord) {
        mCopTask = task;
        mPatrolTask = patrolRecord;
    }

    public SecondaryScreen(Context outerContext, Display display, int theme) {
        super(outerContext, display, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(is1080p) {
            setContentView(R.layout.activity_second_1080p);
        }else {
            setContentView(R.layout.activity_second);
        }
        View contentView = findViewById(R.id.glass_second_layout);
        initView(contentView, mCopTask, mPatrolTask);
        if (null != mListener) {
            mListener.onSecondScreenStart();
        }
    }

    public OnSecondScreenCallback mListener;

    public void setSecondScreenListener(OnSecondScreenCallback listener) {
        mListener = listener;
    }
    public void setScreenSize1080(boolean is1080p) {
        this.is1080p = is1080p;
    }

    public interface OnSecondScreenCallback {
        void onSecondScreenStart();
    }
}
