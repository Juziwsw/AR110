/*
 *  Copyright 2015 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.serenegiant.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Some OpenGL static utility functions.
 */
public class GlUtil {
    private GlUtil() {
    }

    // Assert that no OpenGL ES 2.0 error has been raised.
    public static void checkNoGLES2Error(String msg) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            throw new RuntimeException(msg + ": GLES20 error: " + error);
        }
    }

    public static int createImageTexture(Bitmap bmp) {
        int[] textureHandles = new int[1];
        int textureHandle;

        GLES20.glGenTextures(1, textureHandles, 0);
        textureHandle = textureHandles[0];
        checkNoGLES2Error("glGenTextures");

        // Bind the texture handle to the 2D texture target.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);

        // Configure min/mag filtering, i.e. what scaling method do we use if what we're rendering
        // is smaller or larger than the source image.
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR_MIPMAP_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        checkNoGLES2Error("glTexParameteri");

        // Load the data from the buffer into the texture handle.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, /*level*/ 0, bmp, 0);
        checkNoGLES2Error("texImage2D");

        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        checkNoGLES2Error("glGenerateMipmap");
        return textureHandles[0];
    }

    public static int[] createTextureFormAssets(Context context, String path) {
        int[] result = new int[3];
        InputStream inputStream = null;
        Bitmap bitmap = null;
        try {
            inputStream = context.getAssets().open(path);
            bitmap = BitmapFactory.decodeStream(inputStream);
            result[1] = bitmap.getWidth();
            result[2] = bitmap.getHeight();
            result[0] = GlUtil.createImageTexture(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bitmap != null) {
                bitmap.recycle();
            }
        }

        checkNoGLES2Error("icon createImageTexture");
        return result;
    }

    /**
     * Generate texture with standard parameters.
     */
    public static int generateTexture(int target) {
        final int textureArray[] = new int[1];
        GLES20.glGenTextures(1, textureArray, 0);
        final int textureId = textureArray[0];
        GLES20.glBindTexture(target, textureId);
        GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        checkNoGLES2Error("generateTexture");
        return textureId;
    }

    public static int generateFrameBuffer(int textureId) {
        final int frameBufferArray[] = new int[1];
        GLES20.glGenFramebuffers(1, frameBufferArray, 0);
        final int frameBufferId = frameBufferArray[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureId, 0);
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);

        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            return -1;
        }

        checkNoGLES2Error("generateFrameBuffer");
        return frameBufferId;
    }
}
