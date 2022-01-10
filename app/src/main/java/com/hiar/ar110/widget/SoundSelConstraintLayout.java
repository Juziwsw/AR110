package com.hiar.ar110.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;


import com.blankj.utilcode.util.Utils;
import com.hiar.ar110.R;
import com.hiar.ar110.util.Util;

public class SoundSelConstraintLayout extends FrameLayout {
    private TextView mTextTitle;
    private TextView mTextSoundAll;
    private TextView mTextSoundImportant;
    private TextView mTextCancel;
    private String mSoundType = "出警声音提醒";

    public static final String KEY_COPTASK_SOUND_SET = "key_coptask_sound";
    public static final String KEY_PATROL_SOUND_SET = "key_patrol_sound";
    public static final String KEY_POPULATION_SOUND_SET = "key_population_sound_set";

    public static final int SOUND_ALARM_TYPE_ALL = 1;
    public static final int SOUND_ALARM_TYPE_IMPORTANT = 2;

    public static final String ALARM_SOUND_TYPE_COPTASK = "出警声音提醒";
    public static final String ALARM_SOUND_TYPE_PATROL = "巡逻声音提醒";
    public static final String ALARM_SOUND_TYPE_MOBILE = "流动清查声音提醒";

    public void setSoundTitle(String title) {
        mSoundType = title;
        mTextTitle.setText(title);
    }

    public SoundSelConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SoundSelConstraintLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_sound_selection, this);
        mTextTitle = findViewById(R.id.text_sound_title);
        mTextSoundAll = findViewById(R.id.text_sound_all);
        mTextSoundAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundSelConstraintLayout.this.setVisibility(View.GONE);
                if(mListener != null) {
                    mListener.onSoundTypeChanged(mSoundType, SoundSelConstraintLayout.SOUND_ALARM_TYPE_ALL);
                }

                if(mSoundType.equals(ALARM_SOUND_TYPE_COPTASK)) {
                    Util.setIntPref(Utils.getApp(), SoundSelConstraintLayout.KEY_COPTASK_SOUND_SET, SoundSelConstraintLayout.SOUND_ALARM_TYPE_ALL);
                } else if(mSoundType.equals(ALARM_SOUND_TYPE_PATROL)) {
                    Util.setIntPref(Utils.getApp(), SoundSelConstraintLayout.KEY_PATROL_SOUND_SET, SoundSelConstraintLayout.SOUND_ALARM_TYPE_ALL);
                } else if(mSoundType.equals(ALARM_SOUND_TYPE_MOBILE)) {
                    Util.setIntPref(Utils.getApp(), SoundSelConstraintLayout.KEY_POPULATION_SOUND_SET, SoundSelConstraintLayout.SOUND_ALARM_TYPE_ALL);
                }
            }
        });

        mTextSoundImportant = findViewById(R.id.text_sound_important);
        mTextSoundImportant.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundSelConstraintLayout.this.setVisibility(View.GONE);
                if(mListener != null) {
                    mListener.onSoundTypeChanged(mSoundType, SoundSelConstraintLayout.SOUND_ALARM_TYPE_IMPORTANT);
                }

                if(mSoundType.equals(ALARM_SOUND_TYPE_COPTASK)) {
                    Util.setIntPref(Utils.getApp(), SoundSelConstraintLayout.KEY_COPTASK_SOUND_SET, SoundSelConstraintLayout.SOUND_ALARM_TYPE_IMPORTANT);
                } else if(mSoundType.equals(ALARM_SOUND_TYPE_PATROL)) {
                    Util.setIntPref(Utils.getApp(), SoundSelConstraintLayout.KEY_PATROL_SOUND_SET, SoundSelConstraintLayout.SOUND_ALARM_TYPE_IMPORTANT);
                } else if(mSoundType.equals(ALARM_SOUND_TYPE_MOBILE)) {
                    Util.setIntPref(Utils.getApp(), SoundSelConstraintLayout.KEY_POPULATION_SOUND_SET, SoundSelConstraintLayout.SOUND_ALARM_TYPE_IMPORTANT);
                }
            }
        });

        mTextCancel = findViewById(R.id.text_sound_cancel);

        mTextCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundSelConstraintLayout.this.setVisibility(View.GONE);
            }
        });
    }

    public SoundSelConstraintLayout(Context context) {
        super(context);
    }

    public void setOnSoundChangeListener(OnSoundTypeChangeListener l) {
        mListener = l;
    }

    private OnSoundTypeChangeListener mListener = null;

    public interface OnSoundTypeChangeListener {
        public void onSoundTypeChanged(String alarmType, int soundTy);
    }
}
