package com.serenegiant.widget;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.serenegiant.glutils.GLHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * author: liwf
 * date: 2021/5/13 11:49
 */
class GLDrawer2D {
    private static final float[] VERTICES = new float[]{1.0F, 1.0F, -1.0F, 1.0F, 1.0F, -1.0F, -1.0F, -1.0F};
    private static final float[] TEXCOORD = new float[]{1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F};
    private final int VERTEX_NUM;
    private final int VERTEX_SZ;
    private final FloatBuffer pVertex;
    private final FloatBuffer pTexCoord;
    private final int mTexTarget;
    private int hProgram;
    int maPositionLoc;
    int maTextureCoordLoc;
    int muMVPMatrixLoc;
    int muTexMatrixLoc;
    private final float[] mMvpMatrix;

    public GLDrawer2D(boolean isOES) {
        this(VERTICES, TEXCOORD, isOES);
    }

    public GLDrawer2D(float[] vertices, float[] texcoord, boolean isOES) {
        mMvpMatrix = new float[16];
        VERTEX_NUM = Math.min(vertices != null ? vertices.length : 0, texcoord != null ? texcoord.length : 0) / 2;
        VERTEX_SZ = VERTEX_NUM * 2;
        mTexTarget = isOES ? GLES11Ext.GL_TEXTURE_EXTERNAL_OES : GLES20.GL_TEXTURE_2D;
        pVertex = ByteBuffer.allocateDirect(VERTEX_SZ * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        pVertex.put(vertices);
        pVertex.flip();
        pTexCoord = ByteBuffer.allocateDirect(VERTEX_SZ * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        pTexCoord.put(texcoord);
        pTexCoord.flip();
        if (isOES) {
            hProgram = GLHelper.loadShader("#version 100\nuniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrix;\nattribute highp vec4 aPosition;\nattribute highp vec4 aTextureCoord;\nvarying highp vec2 vTextureCoord;\nvoid main() {\n    gl_Position = uMVPMatrix * aPosition;\n    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n}\n", "#version 100\n#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform samplerExternalOES sTexture;\nvarying highp vec2 vTextureCoord;\nvoid main() {\n  gl_FragColor = texture2D(sTexture, vTextureCoord);\n}");
        } else {
            hProgram = GLHelper.loadShader("#version 100\nuniform mat4 uMVPMatrix;\nuniform mat4 uTexMatrix;\nattribute highp vec4 aPosition;\nattribute highp vec4 aTextureCoord;\nvarying highp vec2 vTextureCoord;\nvoid main() {\n    gl_Position = uMVPMatrix * aPosition;\n    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n}\n", "#version 100\nprecision mediump float;\nuniform sampler2D sTexture;\nvarying highp vec2 vTextureCoord;\nvoid main() {\n  gl_FragColor = texture2D(sTexture, vTextureCoord);\n}");
        }

        Matrix.setIdentityM(mMvpMatrix, 0);
        init();
    }

    public void release() {
        if (hProgram >= 0) {
            GLES20.glDeleteProgram(hProgram);
        }

        hProgram = -1;
    }

    public void setMvpMatrix(float[] matrix, int offset) {
        System.arraycopy(matrix, offset, this.mMvpMatrix, 0, 16);
    }

    public synchronized void draw(int texId, float[] tex_matrix, int offset) {
        draw(texId, tex_matrix, offset, false);
    }

    public synchronized void draw(int texId, float[] tex_matrix, int offset, boolean blend) {
        if (hProgram >= 0) {
            GLES20.glUseProgram(hProgram);
            if (blend) {
                GLES20.glEnable(GLES20.GL_BLEND);
                GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            }
            if (tex_matrix != null) {
                GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, tex_matrix, offset);
            }
            GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mMvpMatrix, 0);
            GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
            GLES20.glVertexAttribPointer(maTextureCoordLoc, 2, GLES20.GL_FLOAT, false, VERTEX_SZ, pTexCoord);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(mTexTarget, texId);
            GLES20.glDrawArrays(5, 0, VERTEX_NUM);
            GLES20.glBindTexture(mTexTarget, 0);
            GLES20.glDisableVertexAttribArray(maTextureCoordLoc);
            if (blend) {
                GLES20.glDisable(GLES20.GL_BLEND);
            }
            GLES20.glUseProgram(0);
        }
    }

    public int initTex() {
        return GLHelper.initTex(mTexTarget, 9728);
    }

    public void deleteTex(int hTex) {
        GLHelper.deleteTex(hTex);
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
}
