package com.hiar.ar110.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.TextView;


import com.blankj.utilcode.util.Utils;
import com.hiar.ar110.R;
import com.hiar.ar110.util.Util;

import java.lang.reflect.Field;

public class FaceThreshSelector extends FrameLayout {
    private NumberPicker mNumpickerFace;
    private TextView mTextConfirm;
    private TextView mTextCancel;
    private float mValidThresh = Util.DEF_FACE_THRESHOOD;

    public FaceThreshSelector(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private TextView mTextThreshood = null;

    public void setTextView(TextView textView) {
        mTextThreshood = textView;
        if(null != mTextThreshood) {
            mTextThreshood.setText((int)mValidThresh+"");
        }
    }

    private void hideNumberPickerDivider(NumberPicker numberPicker) {
        try{
            Field dividerField = numberPicker.getClass().getDeclaredField("mSelectionDivider");
            dividerField.setAccessible(true);
            dividerField.set(numberPicker,null);
            numberPicker.invalidate();
        } catch(NoSuchFieldException | IllegalAccessException | IllegalArgumentException e){

        }
    }

    /*private static final String[] mDisplayValue = {
        "8        0", "8        1", "8        2", "8        3", "8        4",
        "8        5", "8        6", "8        7", "8        8", "8        9",
        "9        0", "9        1", "9        2", "9        3", "9        4", "9       5"
    };*/

    public FaceThreshSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_face_thresh, this);
        mValidThresh = Util.getIntPref(Utils.getApp(), Util.KEY_THRESH, Util.DEF_FACE_THRESHOOD);
        mNumpickerFace = findViewById(R.id.face_thresh_num);
        mNumpickerFace.setMaxValue((int) Util.MAX_FACE_THRESHOOD);
        mNumpickerFace.setMinValue((int)Util.MIN_FACE_THRESHOOD);
        int defVal = Util.getIntPref(Utils.getApp(), Util.KEY_THRESH, Util.DEF_FACE_THRESHOOD);
        mNumpickerFace.setValue((int)defVal);
        mNumpickerFace.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mValidThresh = newVal;
            }
        });

        hideNumberPickerDivider(mNumpickerFace);
        //mNumpickerFace.setDisplayedValues(mDisplayValue);
        mTextConfirm = findViewById(R.id.text_face_confirm);
        mTextConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FaceThreshSelector.this.setVisibility(View.GONE);
                if(null != mTextThreshood) {
                    mTextThreshood.setText((int)mValidThresh+"");
                }
                Util.setIntPref(Utils.getApp(), Util.KEY_THRESH, (int) mValidThresh);
            }
        });

        mTextCancel = findViewById(R.id.text_face_cancel);
        mTextCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FaceThreshSelector.this.setVisibility(View.GONE);
            }
        });
    }
}
