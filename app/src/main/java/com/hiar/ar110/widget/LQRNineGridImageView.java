package com.hiar.ar110.widget;

import android.content.Context;
import android.content.res.TypedArray;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hiar.ar110.R;
import com.hiar.ar110.adapter.LQRNineGridImageViewAdapter;
import com.hiar.mybaselib.utils.AR110Log;
import com.hiscene.imui.widget.NiceImageView;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("unchecked")
public class LQRNineGridImageView<T> extends CardView {
    private static final String TAG = "LQRNineGridImageView";

    private int mRowCount;//行数
    private int mColumnCount;//列数

    private int mMaxSize = 4;
    private int mGap;//宫格间距

    private int mWidth;//当前组件宽度
    private int mHeight;//当前组件高度

    private CopyOnWriteArrayList<NiceImageView> mImageViewList = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<T> mImgDataList;
    private LQRNineGridImageViewAdapter<T> mAdapter;

    public LQRNineGridImageView(Context context) {
        super(context, null);
    }

    public LQRNineGridImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LQRNineGridImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LQRNineGridImageView);
        this.mGap = (int) typedArray.getDimension(R.styleable.LQRNineGridImageView_imgGap, 8);
        typedArray.recycle();
    }

    /**
     * 设置控件的宽高
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = measureWidth(widthMeasureSpec);
        mHeight = measureHeight(heightMeasureSpec);

        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutChildrenView();
    }

    private int measureWidth(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = 200;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private int measureHeight(int measureSpec) {
        int result;

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = 200;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    /**
     * 为子ImageView布局
     */
    private void layoutChildrenView() {
        if (mImgDataList == null) {
            return;
        }
        int childrenCount = mImgDataList.size();
        for (int i = 0; i < childrenCount; i++) {
            NiceImageView childrenView = (NiceImageView) getChildAt(i);

            int rowNum = i / mColumnCount;//当前行数
            int columnNum = i % mColumnCount;//当前列数

            int mImageSize = (mWidth - (mColumnCount + 1) * mGap) / mColumnCount;//图片心坟

            int t_center = (mHeight + mGap) / 2;//中间置以下的顶点（有宫格间距）
            int b_center = (mHeight - mGap) / 2;//中间位置以上的底部（有宫格间距）
            int l_center = (mWidth + mGap) / 2;//中间位置以右的左部（有宫格间距）
            int r_center = (mWidth - mGap) / 2;//中间位置以左的右部（有宫格间距）
            int center = (mHeight - mImageSize) / 2;//中间位置以上顶部（无宫格间距）

            int left = mImageSize * columnNum + mGap * (columnNum + 1);
            int top = mImageSize * rowNum + mGap * (rowNum + 1);
            int right = left + mImageSize;
            int bottom = top + mImageSize;

            /**
             * 不同子view情况下的不同显示
             */
            if (childrenCount == 1) {
                childrenView.setBorderShow(true);
                childrenView.setBorderWidth(1);
                childrenView.setBorderColor(ActivityCompat.getColor(childrenView.getContext(), R.color.avatar_border_color));
                childrenView.isCircle(true);
                childrenView.layout(left, top, right, bottom);
            } else if (childrenCount == 2) {
                if (i == 0) {
                    childrenView.setCornerLeftRadius(45);
                } else {
                    childrenView.setCornerRightRadius(45);
                }
                childrenView.layout(left, top, right, bottom + mImageSize + mGap);
            } else if (childrenCount == 3) {
                if (i == 0) {
                    childrenView.setCornerTopLeftRadius(45);
                } else if (i == 1) {
                    childrenView.setCornerRightRadius(45);
                } else {
                    childrenView.setCornerBottomLeftRadius(45);
                }
                if (i == 1) {
                    childrenView.layout(left, top, right, bottom + mImageSize + mGap);
                } else {
                    childrenView.layout(left, top, right, bottom);
                }
            } else if (childrenCount == 4) {
                if (i == 0) {
                    childrenView.setCornerTopLeftRadius(45);
                } else if (i == 1) {
                    childrenView.setCornerTopRightRadius(45);
                } else if (i == 2) {
                    childrenView.setCornerBottomLeftRadius(45);
                } else {
                    childrenView.setCornerBottomRightRadius(45);
                }
                childrenView.layout(left, top, right, bottom);
            } else if (childrenCount == 5) {
                if (i == 0) {
                    childrenView.layout(r_center - mImageSize, r_center - mImageSize, r_center, r_center);
                } else if (i == 1) {
                    childrenView.layout(l_center, r_center - mImageSize, l_center + mImageSize, r_center);
                } else {
                    childrenView.layout(mGap * (i - 1) + mImageSize * (i - 2), t_center, mGap * (i - 1) + mImageSize * (i - 1), t_center + mImageSize);
                }
            } else if (childrenCount == 6) {
                if (i < 3) {
                    childrenView.layout(mGap * (i + 1) + mImageSize * i, b_center - mImageSize, mGap * (i + 1) + mImageSize * (i + 1), b_center);
                } else {
                    childrenView.layout(mGap * (i - 2) + mImageSize * (i - 3), t_center, mGap * (i - 2) + mImageSize * (i - 2), t_center + mImageSize);
                }
            } else if (childrenCount == 7) {
                if (i == 0) {
                    childrenView.layout(center, mGap, center + mImageSize, mGap + mImageSize);
                } else if (i > 0 && i < 4) {
                    childrenView.layout(mGap * i + mImageSize * (i - 1), center, mGap * i + mImageSize * i, center + mImageSize);
                } else {
                    childrenView.layout(mGap * (i - 3) + mImageSize * (i - 4), t_center + mImageSize / 2, mGap * (i - 3) + mImageSize * (i - 3), t_center + mImageSize / 2 + mImageSize);
                }
            } else if (childrenCount == 8) {
                if (i == 0) {
                    childrenView.layout(r_center - mImageSize, mGap, r_center, mGap + mImageSize);
                } else if (i == 1) {
                    childrenView.layout(l_center, mGap, l_center + mImageSize, mGap + mImageSize);
                } else if (i > 1 && i < 5) {
                    childrenView.layout(mGap * (i - 1) + mImageSize * (i - 2), center, mGap * (i - 1) + mImageSize * (i - 1), center + mImageSize);
                } else {
                    childrenView.layout(mGap * (i - 4) + mImageSize * (i - 5), t_center + mImageSize / 2, mGap * (i - 4) + mImageSize * (i - 4), t_center + mImageSize / 2 + mImageSize);
                }
            } else if (childrenCount == 9) {
                childrenView.layout(left, top, right, bottom);
            }

            if (mAdapter != null) {
                mAdapter.onDisplayImage(getContext(), childrenView, mImgDataList.get(i));
            }
        }
    }

    /**
     * 设置图片数据
     *
     * @param data 图片数据集合
     */
    public void setImagesData(List data) {
        if (data == null || data.isEmpty()) {
            this.setVisibility(GONE);
            return;
        } else {
            this.setVisibility(VISIBLE);
        }

        if (mMaxSize > 0 && data.size() > mMaxSize) {
            data = data.subList(0, mMaxSize);
        }

        int[] gridParam = calculateGridParam(data.size());
        mRowCount = gridParam[0];
        mColumnCount = gridParam[1];
        if (mImgDataList == null) {
            int i = 0;
            while (i < data.size()) {
                NiceImageView iv = getImageView(i);
                if (iv == null)
                    return;
                addViewInLayout(iv, i, generateDefaultLayoutParams());
                i++;
            }
        } else {
            int oldViewCount = mImgDataList.size();
            int newViewCount = data.size();
            if (oldViewCount > newViewCount) {
                removeViewsInLayout(newViewCount, oldViewCount - newViewCount);
            } else if (oldViewCount < newViewCount) {
                for (int i = oldViewCount; i < newViewCount; i++) {
                    ImageView iv = getImageView(i);
                    if (iv == null) {
                        return;
                    }
                    if (iv.getParent() != null) {
                        ((ViewGroup) iv.getParent()).removeView(iv);
                    }
                    addViewInLayout(iv, i, generateDefaultLayoutParams());
                }
            }
        }
        mImgDataList = new CopyOnWriteArrayList(data.toArray());
        requestLayout();
    }

    /**
     * 设置适配器
     *
     * @param adapter 适配器
     */
    public void setAdapter(LQRNineGridImageViewAdapter adapter) {
        mAdapter = adapter;
    }

    /**
     * 设置宫格间距
     *
     * @param gap 宫格间距 px
     */
    public void setGap(int gap) {
        mGap = gap;
    }

    /**
     * 设置宫格参数
     *
     * @param imagesSize 图片数量
     * @return 宫格参数 gridParam[0] 宫格行数 gridParam[1] 宫格列数
     */
    private static int[] calculateGridParam(int imagesSize) {
        int[] gridParam = new int[2];
        if (imagesSize < 3) {
            gridParam[0] = 1;
            gridParam[1] = imagesSize;
        } else if (imagesSize <= 4) {
            gridParam[0] = 2;
            gridParam[1] = 2;
        } else {
            gridParam[0] = imagesSize / 3 + (imagesSize % 3 == 0 ? 0 : 1);
            gridParam[1] = 3;
        }
        return gridParam;
    }

    /**
     * 获得 ImageView
     * 保证了ImageView的重用
     *
     * @param position 位置
     */
    private NiceImageView getImageView(final int position) {
        if (position < mImageViewList.size()) {
            return mImageViewList.get(position);
        } else {
            if (mAdapter != null) {
                NiceImageView imageView = mAdapter.generateImageView(getContext());
                imageView.setBorderColor(ActivityCompat.getColor(getContext(), R.color.avatar_border_color));
                mImageViewList.add(imageView);
                return imageView;
            } else {
                AR110Log.e(TAG, "你必须为LQRNineGridImageView设置LQRNineGridImageViewAdapter");
                return null;
            }
        }
    }
}
