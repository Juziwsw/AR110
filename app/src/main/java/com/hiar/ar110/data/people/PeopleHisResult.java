package com.hiar.ar110.data.people;

public class PeopleHisResult {
    public int retCode;
    public FaceRecogHisData data;

    public static class FaceRecogHisData {
        public String imageUrl;
        public FaceCompareBaseInfo[][] recoFace;
        public FaceCompareBaseInfo[][] unrecoFace;
    }
}
