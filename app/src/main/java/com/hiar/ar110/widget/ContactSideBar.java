package com.hiar.ar110.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.hiar.ar110.R;

import java.util.Arrays;
import java.util.List;

/**
 * @author: liwenfei.
 * data: 2018/7/16 17:23.
 */
public class ContactSideBar extends View {
    private final String TAG = getClass().getSimpleName();
    // 计算波浪贝塞尔曲线的角弧长值
    private static final double ANGLE = Math.PI * 45 / 180;
    private static final double ANGLE_R = Math.PI * 90 / 180;
    private OnTouchLetterChangeListener mListener;
    private int mState = MotionEvent.ACTION_OUTSIDE;

    // 渲染字母表
    private List<String> mLetters;

    // 当前选中的位置
    private int mChoosePosition = 0;
    private int mFocusPosition = 0;

    private int mOldPosition;

    private int mNewPosition;

    // 字母列表画笔
    private Paint mLettersPaint = new Paint();

    // 提示字母画笔
    private Paint mTextPaint = new Paint();
    // 波浪画笔
    private Paint mWavePaint = new Paint();
    private Paint mFocusLettersBgPaint = new Paint();

    private int mTextSize;
    private int mHintTextSize;
    private int mTextColor;
    private int mWaveColor;
    private int mTextColorChoose;
    private int mWidth;
    private int mHeight;
    private int mItemHeight;
    private int mPadding;

    // 波浪路径
    private Path mWavePath = new Path();

    // 圆形路径
    private Path mCirclePath = new Path();

    /**
     * 手指滑动的Y点作为中心点
     */
    private int mCenterY;

    /**
     * 贝塞尔曲线的分布半径
     */
    private int mRadius;

    // 圆形半径
    private int mCircleRadius;
    /**
     * 用于过渡效果计算
     */
    private ValueAnimator mRatioAnimator;

    /**
     * 用于绘制贝塞尔曲线的比率
     */
    private float mRatio;

    // 选中字体的坐标
    private float mPointX, mPointY;

    // 圆形中心点X
    private float mCircleCenterX;

    public ContactSideBar(Context context) {
        this(context, null);
    }

    public ContactSideBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContactSideBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mLetters = Arrays.asList(context.getResources().getStringArray(R.array.contactSideBarLetters));

        mWaveColor = Color.parseColor("#bef9b81b");
        mTextColorChoose = ContextCompat.getColor(context, android.R.color.white);
        mTextSize = context.getResources().getDimensionPixelSize(R.dimen.extra_small_text_size);
        mHintTextSize = context.getResources().getDimensionPixelSize(R.dimen.hintTextSize);
        mPadding = context.getResources().getDimensionPixelSize(R.dimen.padding);
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.contactSideBar);
            mTextColor = a.getColor(R.styleable.contactSideBar_textColor, mTextColor);
            mTextColorChoose = a.getColor(R.styleable.contactSideBar_chooseTextColor, mTextColorChoose);
            mTextSize = a.getDimensionPixelSize(R.styleable.contactSideBar_textSize, mTextSize);
            mHintTextSize = a.getDimensionPixelSize(R.styleable.contactSideBar_hintTextSize, mHintTextSize);
            mWaveColor = a.getColor(R.styleable.contactSideBar_side_backgroundColor, mWaveColor);
            mRadius = a.getDimensionPixelSize(R.styleable.contactSideBar_item_radius, context.getResources().getDimensionPixelSize(R.dimen.item_radius));
            mCircleRadius = a.getDimensionPixelSize(R.styleable.contactSideBar_circleRadius, context.getResources().getDimensionPixelSize(R.dimen.circleRadius));
            a.recycle();
        }

        mWavePaint = new Paint();
        mWavePaint.setAntiAlias(true);
        mWavePaint.setStyle(Paint.Style.FILL);
        mWavePaint.setColor(mWaveColor);

        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(mTextColorChoose);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(mHintTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mFocusLettersBgPaint.setColor(Color.parseColor("#334BDD"));
        mFocusLettersBgPaint.setAntiAlias(true);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final float y = event.getY();
        final float x = event.getX();
        mOldPosition = mChoosePosition;
        mNewPosition = (int) (y / mHeight * mLetters.size());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mState = MotionEvent.ACTION_DOWN;
                //限定触摸范围
                if (x < mWidth - 1.5 * mRadius) {
                    return false;
                }
                mCenterY = (int) y;
                startAnimator(1.0f);

                break;
            case MotionEvent.ACTION_MOVE:
                mState = MotionEvent.ACTION_MOVE;
                mCenterY = (int) y;
                if (mOldPosition != mNewPosition) {
                    if (mNewPosition >= 0 && mNewPosition < mLetters.size()) {
                        mChoosePosition = mNewPosition;
                        if (mListener != null) {
                            mListener.onLetterChange(mLetters.get(mNewPosition));
                        }
                    }
                }
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mState = MotionEvent.ACTION_UP;
                startAnimator(0f);
                mChoosePosition = -1;
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mItemHeight = (mHeight - mPadding) / mLetters.size();
        mPointX = mWidth - 1.6f * mTextSize;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制字母列表
        drawLetters(canvas);

        //绘制波浪
//        drawWavePath(canvas);
        //绘制圆
        drawCirclePath(canvas);
        //绘制选中的字体
        drawChooseText(canvas);
    }

    /**
     * 绘制字母列表
     */
    private void drawLetters(Canvas canvas) {
        for (int i = 0; i < mLetters.size(); i++) {
            mLettersPaint.reset();
            if (i == mFocusPosition) {
                mLettersPaint.setColor(mTextColorChoose);
            } else {
                mLettersPaint.setColor(mTextColor);
            }
            mLettersPaint.setAntiAlias(true);
            mLettersPaint.setTextSize(mTextSize);
            mLettersPaint.setTextAlign(Paint.Align.CENTER);

            Paint.FontMetrics fontMetrics = mLettersPaint.getFontMetrics();
            float baseline = Math.abs(-fontMetrics.bottom - fontMetrics.top);

            float pointY = mItemHeight * i + baseline / 2 + mPadding;

            if (i == mFocusPosition) {
                //AR110Log.i(TAG,"focus letter: %s",mLetters.get(i));
//                canvas.drawCircle(mPointX, pointY - baseline * 0.5f, 20, mFocusLettersBgPaint);
            }
            if (i == mChoosePosition) {
                mPointY = pointY;
            }
            canvas.drawText(mLetters.get(i), mPointX, pointY, mLettersPaint);
        }

    }

    /**
     * 绘制选中的字母
     *
     * @param canvas
     */
    private void drawChooseText(Canvas canvas) {
        if (mChoosePosition != -1) {

            // 绘制提示字符
            if (mRatio >= 0.9f) {
                String target = mLetters.get(mChoosePosition);
                Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
                float baseline = Math.abs(-fontMetrics.bottom - fontMetrics.top);
                float x = mCircleCenterX;
                float y = getHeight()/2 + baseline / 2;
                canvas.drawText(target, x, y, mTextPaint);
            }
        }
    }

    /**
     * 绘制波浪
     *
     * @param canvas
     */
    private void drawWavePath(Canvas canvas) {
        mWavePath.reset();
        // 移动到起始点
        mWavePath.moveTo(mWidth, mCenterY - 3 * mRadius);
        //计算上部控制点的Y轴位置
        int controlTopY = mCenterY - 2 * mRadius;

        //计算上部结束点的坐标
        int endTopX = (int) (mWidth - mRadius * Math.cos(ANGLE) * mRatio);
        int endTopY = (int) (controlTopY + mRadius * Math.sin(ANGLE));
        mWavePath.quadTo(mWidth, controlTopY, endTopX, endTopY);

        //计算中心控制点的坐标
        int controlCenterX = (int) (mWidth - 1.8f * mRadius * Math.sin(ANGLE_R) * mRatio);
        int controlCenterY = mCenterY;
        //计算下部结束点的坐标
        int controlBottomY = mCenterY + 2 * mRadius;
        int endBottomX = endTopX;
        int endBottomY = (int) (controlBottomY - mRadius * Math.cos(ANGLE));
        mWavePath.quadTo(controlCenterX, controlCenterY, endBottomX, endBottomY);

        mWavePath.quadTo(mWidth, controlBottomY, mWidth, controlBottomY + mRadius);

        mWavePath.close();
        canvas.drawPath(mWavePath, mWavePaint);
    }

    /**
     * 绘制左边提示的圆
     *
     * @param canvas
     */
    private void drawCirclePath(Canvas canvas) {
        //x轴的移动路径
        mCircleCenterX = (mWidth + mCircleRadius) - (2.0f * mRadius + 2.0f * mCircleRadius) * mRatio;

        mCirclePath.reset();
        mCirclePath.addCircle(mCircleCenterX, getHeight()/2, mCircleRadius, Path.Direction.CW);
        mCirclePath.op(mWavePath, Path.Op.DIFFERENCE);

        mCirclePath.close();
        canvas.drawPath(mCirclePath, mWavePaint);

    }


    private void startAnimator(float value) {
        if (mRatioAnimator == null) {
            mRatioAnimator = new ValueAnimator();
        }
        mRatioAnimator.cancel();
        mRatioAnimator.setFloatValues(value);
        mRatioAnimator.addUpdateListener(value1 -> {

            mRatio = (float) value1.getAnimatedValue();
            //球弹到位的时候，并且点击的位置变了，即点击的时候显示当前选择位置
            if (mRatio == 1f && mOldPosition != mNewPosition) {
                if (mNewPosition >= 0 && mNewPosition < mLetters.size()) {
                    mChoosePosition = mNewPosition;
                    if (mListener != null) {
                        mListener.onLetterChange(mLetters.get(mNewPosition));
                    }
                }
            }
            invalidate();
        });
        mRatioAnimator.start();
    }


    public void setOnTouchLetterChangeListener(OnTouchLetterChangeListener listener) {
        this.mListener = listener;
    }

    public List<String> getLetters() {
        return mLetters;
    }

    public void setLetters(List<String> letters) {
        this.mLetters = letters;
        invalidate();
    }

    public void setFocusLetter(String focusLetter) {
        if (mFocusPosition >= 0 && mFocusPosition <= mLetters.size() - 1) {
            if (!focusLetter.equals(mLetters.get(mFocusPosition))) {
                mFocusPosition = mLetters.indexOf(focusLetter);
                if (mState != MotionEvent.ACTION_MOVE) {
                    invalidate();
                }
            }
        }
    }

    public interface OnTouchLetterChangeListener {
        void onLetterChange(String letter);
    }
}
