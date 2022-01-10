package com.hiar.ar110.data.photo;

public class SceneImageListResult {
    public SceneImageList data;
    public int retCode;

    public boolean isSuccess() {
        return retCode == 0;
    }
}
