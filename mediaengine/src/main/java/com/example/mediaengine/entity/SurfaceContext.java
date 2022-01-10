package com.example.mediaengine.entity;

import android.graphics.SurfaceTexture;

/**
 * @author: liwenfei.
 * data: 2018/8/30 16:02.
 */
public class SurfaceContext {
    private int textureId;
    private SurfaceTexture surfaceTexture;

    public SurfaceContext(SurfaceTexture surfaceTexture, int textureId){
        this.surfaceTexture = surfaceTexture;
        this.textureId = textureId;
    }

    public int getTextureId() {
        return textureId;
    }

    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }
}
