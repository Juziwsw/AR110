package com.hiar.ar110.data;

public class LintuFaceData {
    public LintuFaceData() {

    }

    public static class ResBody {
        public String requestId;
        public boolean success;
        public int errorCode;
        public String errorMessage;
    }

    public static class LoginReqData {
        public String username;
        public String password;
    }

    public static class LoginRes extends ResBody {
        public String code;
        public String message;
        public LoginResData data;
    }

    public static class LoginResData {
        public UserLoginResponse userResponse;
        public String token;
    }


    public static class UserLoginResponse {
        public int id;
        public String username;
        public String email;
        public String mobile;
        public String name;
        public int areaCode;
        public String comment;
        public String customerId;
        public int roleId;
        public String roleName;
        public String areaName;
    }



    public static class CreateLibReqData {
        public String name;
        public int maxCount;
        public String comment;
        public LibIntegrationData integration;
    }

    public static class LibIntegrationData {
        public String id;
        public String name;
    }


    public static class FaceGroup {
        public int id;
        public String name;
        public int maxCount;
        public String comment;
        public LibIntegrationData integration;
    }

    public static class CreateLibResonse extends ResBody {
        public String code;
        public String message;
        public FaceGroup data;
    }


    public static class FaceImportData {
        public String name;
        public String cardId;
        public int typeId;
        public int sex;
        public int hasCriminalRecord;
        public String photo;
        public String comment;
    }

    public static class FaceImportResponse {
        public String code;
        public String message;
        public ImportFace data;
    }

    public static class ImportFace {
        public int id;
        public int faceAssetId;
        public int faceGroupId;
        public String faceAssetUrl;
        public String name;
        public String cardId;
        public String typeId;
        public String sex;
        public int hasCriminalRecord;
        public String comment;
        public String integration;
    }

    public static class FaceCompare1vN {
        public String image;
        public String integrationGroupId;
        public float minMatchingScore;
        public float maxMatchingCount;
    }

    public static class FaceCompare1vNRes extends ResBody {
        public String code;
        public String message;
        public Compare1vNData data;
    }

    public static class Compare1vNData {
        public OneGroupCompareResult[] result;
    }

    public static class OneGroupCompareResult {
        public float score;
        public int faceId;
        public String name;
        public String cardId;
        public int typeId;
        public int faceAssetId;
        public int faceGroupId;
        public String faceAssetUrl;
        public RetIntegration integration;
    }

    public static class RetIntegration {
        public String nation;
        public String category;
        public String mobile;
        public String email;
        public String province;
        public String city;
        public String district;
        public String address;
        public int Sex;

        public String getAddress() {
            return province+district+address;
        }
        public String getGender() {
            return Sex == 1 ? "男":"女";
        }
    }
}
