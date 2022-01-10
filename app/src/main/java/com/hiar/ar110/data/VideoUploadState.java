package com.hiar.ar110.data;

public class VideoUploadState extends FileUploadData {

    public VideoUploadState(int state, String url, int id) {
        mUrl = url;
        isUpload = state;
        mId = id;
    }
}
