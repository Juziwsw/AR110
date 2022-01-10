package com.serenegiant.widget;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.serenegiant.glutils.GLHelper;
import com.serenegiant.util.GlUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * author: liwf
 * date: 2021/5/12 16:34
 */
class GLTimeDrawer2D {
    private static final float[] VERTICES = new float[]{1.0F, 1.0F, -1.0F, 1.0F, 1.0F, -1.0F, -1.0F, -1.0F};
    private final int NUM_SIZE = 12;
    private final int VERTEX_NUM;
    private final int VERTEX_SZ;
    private final FloatBuffer pVertex;
    private FloatBuffer[] pTexCoords = new FloatBuffer[12];
    private FloatBuffer pTexCoord;
    private final int mTexTarget;
    private int hProgram;
    int maPositionLoc;
    int maTextureCoordLoc;
    int muMVPMatrixLoc;
    int muTexMatrixLoc;
    private final float[] mMvpMatrix;
    private int textureId = -1;
    private float[] texMatrix = new float[16];
    private float scaleX, scaleY;

    public GLTimeDrawer2D(boolean isOES, Context context) {
        this(VERTICES, isOES);
        int[] result = GlUtil.createTextureFormAssets(context, "number.png");
        textureId = result[0];
        int textureWidth = result[1];
        int textureHeight = result[2];
        scaleX = 0.1f;
        scaleY = scaleX * textureHeight / textureWidth / NUM_SIZE;
    }

    public GLTimeDrawer2D(float[] vertices, boolean isOES) {
        mMvpMatrix = new float[16];
        VERTEX_NUM = Math.min(vertices != null ? vertices.length : 0, 8) / 2;
        VERTEX_SZ = VERTEX_NUM * 2;
        mTexTarget = isOES ? GLES11Ext.GL_TEXTURE_EXTERNAL_OES : GLES20.GL_TEXTURE_2D;
        pVertex = ByteBuffer.allocateDirect(VERTEX_SZ * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        pVertex.put(vertices);
        pVertex.flip();

        int length = pTexCoords.length;
        for (int i = 0; i < length; i++) {
            FloatBuffer pTexCoord = ByteBuffer.allocateDirect(VERTEX_SZ * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            float top = i * 1.0f / length;
            float bottom = top + 1.0f / length;
            float[] texcoord = new float[]{
                    1.0F, top,
                    0.0F, top,
                    1.0F, bottom,
                    0.0F, bottom};
            pTexCoord.put(texcoord);
            pTexCoord.flip();
            pTexCoords[i] = pTexCoord;
        }

        if (isOES) {
            hProgram = GLHelper.loadShader("#version 100\nuniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrix;\nattribute highp vec4 aPosition;\nattribute highp vec4 aTextureCoord;\nvarying highp vec2 vTextureCoord;\nvoid main() {\n    gl_Position = uMVPMatrix * aPosition;\n    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n}\n", "#version 100\n#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform samplerExternalOES sTexture;\nvarying highp vec2 vTextureCoord;\nvoid main() {\n  gl_FragColor = texture2D(sTexture, vTextureCoord);\n}");
        } else {
            hProgram = GLHelper.loadShader("#version 100\nuniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrix;\nattribute highp vec4 aPosition;\nattribute highp vec4 aTextureCoord;\nvarying highp vec2 vTextureCoord;\nvoid main() {\n    gl_Position = uMVPMatrix * aPosition;\n    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n}\n", "#version 100\nprecision mediump float;\nuniform sampler2D sTexture;\nvarying highp vec2 vTextureCoord;\nvoid main() {\n  gl_FragColor = texture2D(sTexture, vTextureCoord);\n}");
        }

        Matrix.setIdentityM(mMvpMatrix, 0);
        Matrix.setIdentityM(texMatrix, 0);
        init();
    }

    private void init() {
        GLES20.glUseProgram(hProgram);
        maPositionLoc = GLES20.glGetAttribLocation(hProgram, "aPosition");
        maTextureCoordLoc = GLES20.glGetAttribLocation(hProgram, "aTextureCoord");
        muMVPMatrixLoc = GLES20.glGetUniformLocation(hProgram, "uMVPMatrix");
        muTexMatrixLoc = GLES20.glGetUniformLocation(hProgram, "uTexMatrix");
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mMvpMatrix, 0);
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, mMvpMatrix, 0);
        GLES20.glVertexAttribPointer(maPositionLoc, 2, GLES20.GL_FLOAT, false, VERTEX_SZ, pVertex);
        GLES20.glEnableVertexAttribArray(maPositionLoc);
    }

    public void release() {
        if (hProgram >= 0) {
            GLES20.glDeleteProgram(hProgram);
        }
        if (textureId >= 0) {
            GLHelper.deleteTex(textureId);
            textureId = -1;
        }
        hProgram = -1;
    }

    public synchronized void draw(int texId, float[] tex_matrix, int offset) {
        if (hProgram >= 0) {
            GLES20.glUseProgram(hProgram);
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            if (tex_matrix != null) {
                GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, tex_matrix, offset);
            }
            GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
            GLES20.glVertexAttribPointer(maTextureCoordLoc, 2, GLES20.GL_FLOAT, false, VERTEX_SZ, pTexCoord);
            GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mMvpMatrix, 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(mTexTarget, texId);
            GLES20.glDrawArrays(5, 0, VERTEX_NUM);
            GLES20.glBindTexture(mTexTarget, 0);
            GLES20.glDisableVertexAttribArray(maTextureCoordLoc);
            GLES20.glDisable(GLES20.GL_BLEND);
            GLES20.glUseProgram(0);
        }
    }

    public void drawTime() {
        String time = convertTimestamp2Date(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss");
        char[] timeChars = time.toCharArray();
        float transX = 0.3f;
        Matrix.setIdentityM(mMvpMatrix, 0);
        Matrix.translateM(mMvpMatrix, 0, 0.3f, -0.8f, 0);
        Matrix.scaleM(mMvpMatrix, 0, scaleX, scaleY, 1);
        for (char item : timeChars) {
            Matrix.translateM(mMvpMatrix, 0, transX, 0, 0);
            FloatBuffer tempTexCoord;
            switch (item) {
                case '-':
                    tempTexCoord = pTexCoords[10];
                    break;
                case ':':
                    tempTexCoord = pTexCoords[11];
                    break;
                case ' ':
                    tempTexCoord = null;
                    break;
                default:
                    int index = Character.getNumericValue(item);
                    if (index <= 9) {
                        tempTexCoord = pTexCoords[index];
                    } else {
                        tempTexCoord = null;
                    }
                    break;
            }
            if (tempTexCoord != null) {
                pTexCoord = tempTexCoord;
                draw(textureId, null, 0);
            }
        }
    }

    public static String convertTimestamp2Date(Long timestamp, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(new Date(timestamp));
    }
}
