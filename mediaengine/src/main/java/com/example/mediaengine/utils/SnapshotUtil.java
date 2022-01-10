package com.example.mediaengine.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import com.example.mediaengine.entity.FrameData;
import com.hileia.common.utils.XLog;

/**
 * @author hujun
 * @date 18/9/2
 */

public class SnapshotUtil {
    private static final String TAG = "FreezeManagerTAG";

    private RenderScript renderScript;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private int preYuvInLength = 0;
    private int preWidth = 0;
    private int preHeight = 0;
    private Allocation mInAllocation, mOutAllocation;

    public SnapshotUtil(Context context) {
        //耗时操作
        renderScript = RenderScript.create(context);
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(renderScript, Element.U8_4(renderScript));
    }

    public Bitmap freezeData(FrameData frameData) {
        XLog.d(TAG, "freezeData");
        byte[] yuvByteArray = frameData.getData();
        int width = frameData.getWidth();
        int height = frameData.getHeight();
        int format = frameData.getFormat();
        if (yuvByteArray == null || yuvByteArray.length == 0) {
            XLog.e(TAG, "freezeData 冻屏失败");
            return null;
        }

        Allocation in = getYuvAllocationIn(yuvByteArray);
        Allocation out = getAllocationOut(width, height);

        if (format == FrameData.YUV420SP) {
            in.copyFrom(YUVUtil.yuv420_to_yuv420sp(yuvByteArray, width, height));
        } else if (format == FrameData.YUV420) {
            in.copyFrom(yuvByteArray);
        }

        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);

        Bitmap bmpOut = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        out.copyTo(bmpOut);

        if (frameData.isMirror()) {
            bmpOut = mirrorConvert(bmpOut, 0);
        }

        return bmpOut;
    }

    /**
     * //flag: 0 左右翻转，1 上下翻转
     *
     * @param srcBitmap
     * @param flag
     * @return
     */
    public Bitmap mirrorConvert(Bitmap srcBitmap, int flag) {
        Matrix matrix = new Matrix();
        if (flag == 0) //左右翻转
            matrix.setScale(-1, 1);
        if (flag == 1)//上下翻转
            matrix.setScale(1, -1);
        return Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(), srcBitmap.getHeight(), matrix, true);
    }

    private Allocation getYuvAllocationIn(byte[] yuvByteArray) {
        if (mInAllocation == null || yuvByteArray.length != preYuvInLength) {
            preYuvInLength = yuvByteArray.length;
            Type.Builder yuvType = new Type.Builder(renderScript, Element.U8(renderScript)).setX(yuvByteArray.length);
            mInAllocation = Allocation.createTyped(renderScript, yuvType.create(), Allocation.USAGE_SCRIPT);
        }
        return mInAllocation;
    }

    private Allocation getAllocationOut(int width, int height) {
        if (mOutAllocation == null || preWidth != width || preHeight != height) {
            preWidth = width;
            preHeight = height;
            Type.Builder rgbaType = new Type.Builder(renderScript, Element.RGBA_8888(renderScript)).setX(width).setY(height);
            mOutAllocation = Allocation.createTyped(renderScript, rgbaType.create(), Allocation.USAGE_SCRIPT);
        }
        return mOutAllocation;
    }
}
