package com.hiar.ar110.data.audio;

import com.hiar.ar110.data.FileUploadData;

public class AudioUploadStatus extends FileUploadData {

    public AudioUploadStatus(int state, String url, int id) {
        mUrl = url;
        isUpload = state;
        mId = id;
    }
}
