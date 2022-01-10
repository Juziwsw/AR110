package com.hiar.ar110.data;

/**
 * @author wilson.chen
 */
public class FileUploadData {
    public int isUpload;
    public String mUrl;
    public int mId;

    public FileUploadData() {

    }

    public FileUploadData(int state, String url, int id) {
        mUrl = url;
        isUpload = state;
        mId = id;
    }
}
