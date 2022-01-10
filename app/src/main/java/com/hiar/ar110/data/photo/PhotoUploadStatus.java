package com.hiar.ar110.data.photo;

import com.hiar.ar110.data.FileUploadData;

public class PhotoUploadStatus extends FileUploadData {

    public PhotoUploadStatus(int state, String url, int id) {
        mUrl = url;
        isUpload = state;
        mId = id;
    }
}
