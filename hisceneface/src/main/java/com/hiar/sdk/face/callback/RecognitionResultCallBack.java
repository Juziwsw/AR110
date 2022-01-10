package com.hiar.sdk.face.callback;

public interface RecognitionResultCallBack {
   public void execute(long frame_id, long face_id, long user_id, float similarity);
}
