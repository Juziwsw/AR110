package com.hiar.ar110.data.audio;

public  class AudioFileInfo {
    public String mFileName;
    public int mAudioLen;
    public int mUploadStatus;
    public boolean isPlaying = false;
    public int mCurPlayPos = 0;
    public String absPath;

    @Override
    public String toString() {
        return "AudioFileInfo{" +
            "mFileName='" + mFileName + '\'' +
            ", mUploadStatus=" + mUploadStatus +
            '}';
    }
}
