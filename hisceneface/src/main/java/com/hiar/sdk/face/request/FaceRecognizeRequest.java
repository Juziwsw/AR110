package com.hiar.sdk.face.request;


import com.hiar.sdk.face.callback.RecognitionResultCallBack;

public class FaceRecognizeRequest {
    public void send(RecognitionResultCallBack callBack, long frame_id, long face_id, long user_id, float similarity) throws Exception {
        callBack.execute(frame_id, face_id, user_id, similarity);
    }
}
