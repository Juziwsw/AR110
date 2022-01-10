package com.hiar.ar110.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.hiar.ar110.R;
import com.hiar.ar110.util.Util;
import com.hiar.mybaselib.recog.FaceRecognitionInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roly on 20/12/2017.
 * Email: yumm@hiscene.com
 */
public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "MyRelativeLayout";
    private SurfaceHolder holder=null; //控制对象
    private ArrayList<FaceRecognitionInfo> rects = new ArrayList<>();
    private boolean mBeginLoop = false;
    private boolean mNeedDraw = false;
    private Bitmap bitmap;
    private NinePatchDrawable npd = null;
    private float mDensity = 1.5f;
    private float mScale = 1;

    public void clearFaceRect() {
        synchronized (rects) {
            if(rects.size() > 0) {
                rects.clear();
                mNeedDraw = true;
            }
        }
    }

    public void offerRects(List<FaceRecognitionInfo> faces) {
        synchronized (rects) {
            rects.clear();
            rects.addAll(faces);
            mNeedDraw = true;
        }
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDensity = 1.5f;
        holder=getHolder();
        holder.setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
        holder.addCallback(this);
    }

    public void setFaceBitmap(Bitmap bmp) {
        bitmap = bmp;
    }
    public void setmScale(float scale){
        this.mScale=scale;
    }

    public void doDraw(Canvas canvas) {
        super.draw(canvas);
        Paint p=new Paint(); //笔触
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(p);

        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        p.setAntiAlias(true); //反锯齿
        p.setColor(Color.GREEN);
        p.setStrokeWidth(3);
        p.setStyle(Paint.Style.STROKE);
        p.setTextSize(32);
        synchronized (rects) {
            for(int i=0;i<rects.size();i++) {
                Rect rect = null;
                if(!Util.mNeedMultiScreen) {
                    Rect rectFace = rects.get(i).bbox;
                    rect = new Rect();
                    int left = (int)(rectFace.left * mDensity);
                    int right = (int) (rectFace.right * mDensity);
                    right = (right > getWidth()) ? getWidth():right;

                    int top = (int)(rectFace.top * mDensity);
                    top = (top < 0) ? 0 : top;

                    int bottom = (int)(rectFace.bottom * mDensity);
                    bottom = (bottom > getHeight()) ? getHeight():bottom;

                    rect.left = left;
                    rect.right = right;
                    rect.top = top;
                    rect.bottom = bottom;
                } else {
                    rect = rects.get(i).bbox;
                    rect.left =(int)(rect.left* mScale);
                    rect.right =(int)(rect.right* mScale);
                    rect.top = (int)(rect.top* mScale);
                    rect.bottom = (int)(rect.bottom* mScale);
                }


                /*int width = rect.width();
                int height = rect.height();
                rect.left = (int)((rect.left - 80) * 1.3f);
                rect.right = rect.left + width;
                rect.top = (int)((rect.top - 32) * 1.3f);
                rect.bottom = rect.top + height;*/
                //canvas.drawRect(rect, p);
                /*String dispName = ""+rects.get(i).faceId;
                canvas.drawText(dispName,0, dispName.length(), rect.left + (rect.width()-dispName.length()*12)/2
                        , rect.top-20, p);*/

                //设置需要的范围边界
                if(npd == null) {
                    npd = (NinePatchDrawable) getResources()
                            .getDrawable(R.drawable.face_selected);
                }
                npd.setBounds(rect);
                //最后在画布上画出来
                npd.draw(canvas);
            }
        }
    }

    class MyLoop implements Runnable{
        //熟悉游戏编程的应该很面熟吧，主循环
        @Override
        public void run() {
            mBeginLoop = true;
            while(mBeginLoop){
                try{
                    if(mNeedDraw) {
                        Canvas c=holder.lockCanvas();
                        doDraw(c);
                        holder.unlockCanvasAndPost(c);
                        mNeedDraw = false;
                    }

                    Thread.sleep(10);
                } catch(Exception e){

                }
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        new Thread(new MyLoop()).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mBeginLoop = false;
    }
}
