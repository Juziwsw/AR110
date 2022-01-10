/*
 *  UVCCamera
 *  library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2017 saki t_saki@serenegiant.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *  All files in the folder are under this Apache License, Version 2.0.
 *  Files in the libjpeg-turbo, libusb, libuvc, rapidjson folder
 *  may have a different license, see the respective files.
 */

package com.serenegiant.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.hiar.mybaselib.utils.BitmapUtils;
import com.hileia.common.enginer.LeiaBoxEngine;
import com.serenegiant.encoder.IVideoEncoder;
import com.serenegiant.encoder.MediaEncoder;
import com.serenegiant.encoder.MediaVideoEncoder;
import com.serenegiant.glutils.EGLBase;
import com.serenegiant.glutils.es1.GLHelper;
import com.serenegiant.util.GlUtil;

import java.io.File;

public class UVCCameraTextureView extends TextureView    // API >= 14
        implements TextureView.SurfaceTextureListener, CameraViewInterface {

    private static final boolean DEBUG = true;    // TODO set false on release
    private static final String TAG = "UVCCameraTextureView";

    private boolean mHasSurface;
    private RenderHandler mRenderHandler;
    private final Object mCaptureSync = new Object();
    private Bitmap mTempBitmap;
    private boolean mReqesutCaptureStillImage;
    private Callback mCallback;

    public UVCCameraTextureView(final Context context) {
        this(context, null, 0);
    }

    public UVCCameraTextureView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UVCCameraTextureView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        setSurfaceTextureListener(this);
    }

    @Override
    public void onResume() {
        if (DEBUG) Log.v(TAG, "onResume:");
        if (mHasSurface) {
            mRenderHandler = RenderHandler.createHandler(super.getSurfaceTexture(), getContext(), getWidth(), getHeight());
        }
    }

    @Override
    public void onPause() {
        if (DEBUG) Log.v(TAG, "onPause:");
        if (mRenderHandler != null) {
            mRenderHandler.release();
            mRenderHandler = null;
        }
        if (mTempBitmap != null) {
            mTempBitmap.recycle();
            mTempBitmap = null;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width, final int height) {
        if (DEBUG) Log.v(TAG, "onSurfaceTextureAvailable:" + surface);
        if (mRenderHandler == null) {
            mRenderHandler = RenderHandler.createHandler(surface, getContext(), width, height);
        } else {
            mRenderHandler.resize(width, height);
        }
        mHasSurface = true;
        if (mCallback != null) {
            mCallback.onSurfaceCreated(this, getSurface());
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(final SurfaceTexture surface, final int width, final int height) {
        if (DEBUG) Log.v(TAG, "onSurfaceTextureSizeChanged:" + surface);
        if (mRenderHandler != null) {
            mRenderHandler.resize(width, height);
        }
        if (mCallback != null) {
            mCallback.onSurfaceChanged(this, getSurface(), width, height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(final SurfaceTexture surface) {
        if (DEBUG) Log.v(TAG, "onSurfaceTextureDestroyed:" + surface);
        if (mRenderHandler != null) {
            mRenderHandler.release();
            mRenderHandler = null;
        }
        mHasSurface = false;
        if (mCallback != null) {
            mCallback.onSurfaceDestroy(this, getSurface());
        }
        if (mPreviewSurface != null) {
            mPreviewSurface.release();
            mPreviewSurface = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(final SurfaceTexture surface) {
        synchronized (mCaptureSync) {
            if (mReqesutCaptureStillImage) {
                mReqesutCaptureStillImage = false;
                if (mTempBitmap == null)
                    mTempBitmap = getBitmap();
                else
                    getBitmap(mTempBitmap);
                mCaptureSync.notifyAll();
            }
        }
    }

    @Override
    public boolean hasSurface() {
        return mHasSurface;
    }

    @Override
    public Bitmap captureStillImage() {
        synchronized (mCaptureSync) {
            mReqesutCaptureStillImage = true;
            try {
                mCaptureSync.wait();
            } catch (final InterruptedException e) {
            }
            return mTempBitmap;
        }
    }

    @Override
    public SurfaceTexture getSurfaceTexture() {
        return mRenderHandler != null ? mRenderHandler.getPreviewTexture() : null;
    }

    private Surface mPreviewSurface;

    @Override
    public Surface getSurface() {
        if (DEBUG) Log.v(TAG, "getSurface:hasSurface=" + mHasSurface);
        if (mPreviewSurface == null) {
            final SurfaceTexture st = getSurfaceTexture();
            if (st != null) {
                mPreviewSurface = new Surface(st);
            }
        }
        return mPreviewSurface;
    }

    @Override
    public void setVideoEncoder(final IVideoEncoder encoder) {
        if (mRenderHandler != null)
            mRenderHandler.setVideoEncoder(encoder);
    }

    @Override
    public void setCallback(final Callback callback) {
        mCallback = callback;
    }

    private static final class RenderHandler extends Handler
            implements SurfaceTexture.OnFrameAvailableListener {

        private static final int MSG_REQUEST_RENDER = 1;
        private static final int MSG_SET_ENCODER = 2;
        private static final int MSG_CREATE_SURFACE = 3;
        private static final int MSG_RESIZE = 4;
        private static final int MSG_TERMINATE = 9;

        private RenderThread mThread;
        private boolean mIsActive = true;

        public static final RenderHandler createHandler(
                final SurfaceTexture surface, final Context context, final int width, final int height) {

            final RenderThread thread = new RenderThread(surface, context.getApplicationContext(), width, height);
            thread.start();
            return thread.getHandler();
        }

        private RenderHandler(final RenderThread thread) {
            mThread = thread;
        }

        public final void setVideoEncoder(final IVideoEncoder encoder) {
            if (DEBUG) Log.v(TAG, "setVideoEncoder:");
            if (mIsActive)
                sendMessage(obtainMessage(MSG_SET_ENCODER, encoder));
        }

        public final SurfaceTexture getPreviewTexture() {
            if (DEBUG) Log.v(TAG, "getPreviewTexture:");
            if (mIsActive) {
                synchronized (mThread.mSync) {
                    sendEmptyMessage(MSG_CREATE_SURFACE);
                    try {
                        mThread.mSync.wait();
                    } catch (final InterruptedException e) {
                    }
                    return mThread.mPreviewSurface;
                }
            } else {
                return null;
            }
        }

        public void resize(final int width, final int height) {
            if (DEBUG) Log.v(TAG, "resize:");
            if (mIsActive) {
                synchronized (mThread.mSync) {
                    sendMessage(obtainMessage(MSG_RESIZE, width, height));
                    try {
                        mThread.mSync.wait();
                    } catch (final InterruptedException e) {
                    }
                }
            }
        }

        public final void release() {
            if (DEBUG) Log.v(TAG, "release:");
            if (mIsActive) {
                mIsActive = false;
                removeMessages(MSG_REQUEST_RENDER);
                removeMessages(MSG_SET_ENCODER);
                sendEmptyMessage(MSG_TERMINATE);
            }
        }

        @Override
        public final void onFrameAvailable(final SurfaceTexture surfaceTexture) {
            if (mIsActive) {
                sendEmptyMessage(MSG_REQUEST_RENDER);
            }
        }

        @Override
        public final void handleMessage(final Message msg) {
            if (mThread == null) return;
            switch (msg.what) {
                case MSG_REQUEST_RENDER:
                    mThread.onDrawFrame();
                    break;
                case MSG_SET_ENCODER:
                    mThread.setEncoder((MediaEncoder) msg.obj);
                    break;
                case MSG_CREATE_SURFACE:
                    mThread.updatePreviewSurface();
                    break;
                case MSG_RESIZE:
                    mThread.resize(msg.arg1, msg.arg2);
                    break;
                case MSG_TERMINATE:
                    Looper.myLooper().quit();
                    mThread = null;
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

        private static final class RenderThread extends Thread {
            private final Object mSync = new Object();
            private final SurfaceTexture mSurface;
            private RenderHandler mHandler;
            private EGLBase mEgl, mPreviewEgl;
            /**
             * IEglSurface instance related to this TextureView
             */
            private EGLBase.IEglSurface mEglSurface;
            private EGLBase.IEglSurface mOffScreenSurface;
            private GLDrawer2D mOESDrawer, mRGBDrawer, mNameDrawer;
            private GLTimeDrawer2D mTimeDrawer;
            private int mOESTexId = -1;
            private int mRGBTexId = -1;
            private int mFrameBufferId = -1;
            private int mNameTexId = -1;
            /**
             * SurfaceTexture instance to receive video images
             */
            private SurfaceTexture mPreviewSurface;
            private final float[] mStMatrix = new float[16];
            private final float[] mIdentityMatrix = new float[16];
            private MediaEncoder mEncoder;
            private int mViewWidth, mViewHeight;
            private Context mContext;
            private final int INTERVAL = 50;//帧渲染时间间隔
            private long lastFrameTime = 0;

            /**
             * constructor
             *
             * @param surface: drawing surface came from TexureView
             */
            public RenderThread(final SurfaceTexture surface, final Context context, final int width, final int height) {
                mSurface = surface;
                mViewWidth = width;
                mViewHeight = height;
                mContext = context;
                setName("RenderThread");
                Matrix.setIdentityM(mIdentityMatrix, 0);
            }

            public final RenderHandler getHandler() {
                if (DEBUG) Log.v(TAG, "RenderThread#getHandler:");
                synchronized (mSync) {
                    // create rendering thread
                    if (mHandler == null)
                        try {
                            mSync.wait();
                        } catch (final InterruptedException e) {
                        }
                }
                return mHandler;
            }

            public void resize(final int width, final int height) {
                if (((width > 0) && (width != mViewWidth)) || ((height > 0) && (height != mViewHeight))) {
                    mViewWidth = width;
                    mViewHeight = height;
                    updatePreviewSurface();
                } else {
                    synchronized (mSync) {
                        mSync.notifyAll();
                    }
                }
            }

            public final void updatePreviewSurface() {
                if (DEBUG) Log.i(TAG, "RenderThread#updatePreviewSurface:");
                synchronized (mSync) {
                    if (mPreviewSurface != null) {
                        if (DEBUG) Log.d(TAG, "updatePreviewSurface:release mPreviewSurface");
                        mPreviewSurface.setOnFrameAvailableListener(null);
                        mPreviewSurface.release();
                        mPreviewSurface = null;
                    }
                    mOffScreenSurface.makeCurrent();
                    if (mOESTexId >= 0) {
                        mOESDrawer.deleteTex(mOESTexId);
                    }
                    // create texture and SurfaceTexture for input from camera
                    mOESTexId = mOESDrawer.initTex();
                    if (DEBUG) Log.v(TAG, "updatePreviewSurface:tex_id=" + mOESTexId);
                    mPreviewSurface = new SurfaceTexture(mOESTexId);
                    mPreviewSurface.setDefaultBufferSize(mViewWidth, mViewHeight);
                    mPreviewSurface.setOnFrameAvailableListener(mHandler);
                    // notify to caller thread that previewSurface is ready
                    mSync.notifyAll();
                }
            }

            public final void setEncoder(final MediaEncoder encoder) {
                if (DEBUG) Log.v(TAG, "RenderThread#setEncoder:encoder=" + encoder);
                if (encoder != null && (encoder instanceof MediaVideoEncoder)) {
                    ((MediaVideoEncoder) encoder).setEglContext(mEgl.getContext(), mRGBTexId);
                }
                mEncoder = encoder;
            }

            public final void onDrawFrame() {
                mOffScreenSurface.makeCurrent();
                // update texture(came from camera)
                mPreviewSurface.updateTexImage();
                // get texture matrix
                mPreviewSurface.getTransformMatrix(mStMatrix);
                long currentTime = System.currentTimeMillis();
                if ((currentTime - lastFrameTime) >= INTERVAL) {
                    lastFrameTime = currentTime;
                    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferId);
                    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                    GLES20.glViewport(0, 0, mViewWidth, mViewHeight);
                    mOESDrawer.draw(mOESTexId, mStMatrix, 0);
                    mTimeDrawer.drawTime();
                    mNameDrawer.draw(mNameTexId, mStMatrix, 0, true);
                    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
                    // notify video encoder if it exist
                    if (mEncoder != null) {
                        // notify to capturing thread that the camera frame is available.
                        if (mEncoder instanceof MediaVideoEncoder)
                            ((MediaVideoEncoder) mEncoder).frameAvailableSoon(mIdentityMatrix);
                        else
                            mEncoder.frameAvailableSoon();
                    }
                    drawToPreview();
                }
            }

            // draw to preview screen
            private void drawToPreview() {
                if (mPreviewEgl == null) {
                    mPreviewEgl = EGLBase.createFrom(mEgl.getContext(), false, false);
                }
                if (mEglSurface == null) {
                    mEglSurface = mPreviewEgl.createFromSurface(mSurface);
                }
                if (mRGBDrawer == null) {
                    mRGBDrawer = new GLDrawer2D(false);
                }
                mEglSurface.makeCurrent();
                GLES20.glViewport(0, 0, mViewWidth, mViewHeight);
                mRGBDrawer.draw(mRGBTexId, mIdentityMatrix, 0);
                mEglSurface.swap();
            }

            @Override
            public final void run() {
                Log.d(TAG, getName() + " started");
                init();
                Looper.prepare();
                synchronized (mSync) {
                    mHandler = new RenderHandler(this);
                    mSync.notify();
                }

                Looper.loop();

                Log.d(TAG, getName() + " finishing");
                release();
                synchronized (mSync) {
                    mHandler = null;
                    mSync.notify();
                }
            }

            private final void init() {
                if (DEBUG) Log.v(TAG, "RenderThread#init:");
                mEgl = EGLBase.createFrom(null, false, false);
                mOffScreenSurface = mEgl.createOffscreen(1, 1);
                mOffScreenSurface.makeCurrent();
                mOESDrawer = new GLDrawer2D(true);
                mTimeDrawer = new GLTimeDrawer2D(false, mContext);
                mNameDrawer = new GLDrawer2D(false);
                if (mRGBTexId < 0) {
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                    mRGBTexId = GlUtil.generateTexture(GLES20.GL_TEXTURE_2D);
                    GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mViewWidth, mViewHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
                    mFrameBufferId = GlUtil.generateFrameBuffer(mRGBTexId);
                }
                if (mNameTexId < 0) {
                    Bitmap nameImage = BitmapUtils.getBitmapFromFile(LeiaBoxEngine.getInstance().settingManager().getRootDir() + File.separator + "name.png");
                    if (nameImage != null) {
                        mNameTexId = GlUtil.createImageTexture(nameImage);
                        float[] mvpMatrix = new float[16];
                        Matrix.setIdentityM(mvpMatrix, 0);
                        Matrix.translateM(mvpMatrix, 0, -0.7f, -0.8f, 0);
                        float scaleX = 0.15f;
                        float scaleY = scaleX * nameImage.getHeight() / nameImage.getWidth();
                        Matrix.scaleM(mvpMatrix, 0, scaleX, scaleY, 1);
                        mNameDrawer.setMvpMatrix(mvpMatrix, 0);
                    }
                }
                Log.i(TAG, String.format("mRGBTexId: %d , mFrameBufferId %d, mViewWidth: %d, mViewHeight: %d", mRGBTexId, mFrameBufferId, mViewWidth, mViewHeight));
            }

            private final void release() {
                if (DEBUG) Log.v(TAG, "RenderThread#release:");
                if (mOESDrawer != null) {
                    mOESDrawer.release();
                    mOESDrawer = null;
                }
                if (mRGBDrawer != null) {
                    mRGBDrawer.release();
                    mRGBDrawer = null;
                }
                if (mTimeDrawer != null) {
                    mTimeDrawer.release();
                    mTimeDrawer = null;
                }
                if (mNameDrawer != null) {
                    mNameDrawer.release();
                    mNameDrawer = null;
                }
                if (mPreviewSurface != null) {
                    mPreviewSurface.release();
                    mPreviewSurface = null;
                }
                if (mOESTexId >= 0) {
                    GLHelper.deleteTex(mOESTexId);
                    mOESTexId = -1;
                }
                if (mRGBTexId >= 0) {
                    GLHelper.deleteTex(mRGBTexId);
                    mRGBTexId = -1;
                }
                if (mNameTexId >= 0) {
                    GLHelper.deleteTex(mNameTexId);
                    mNameTexId = -1;
                }
                if (mEglSurface != null) {
                    mEglSurface.release();
                    mEglSurface = null;
                }
                if (mEgl != null) {
                    mEgl.release();
                    mEgl = null;
                }
            }
        }
    }
}
