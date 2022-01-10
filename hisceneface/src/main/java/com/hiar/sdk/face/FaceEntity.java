package com.hiar.sdk.face;

public class FaceEntity {
    /**
     * 人脸id，主键
     */
    private long id;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 图片路径
     */
    private String imagePath;

    /**
     * 人脸特征数据
     */
    private byte[] feature;

    /**
     * 注册时间
     */
    private long registerTime;

    /**
     * 人脸信息描述
     */
    private String description;

    public FaceEntity(String userName, String imagePath, byte[] feature, String description) {
        this.userName = userName;
        this.imagePath = imagePath;
        this.feature = feature;
        this.registerTime = System.currentTimeMillis();
        this.description = description;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setFeature(byte[] feature) {
        this.feature = feature;
    }

    public byte[] getFeature() {
        return feature;
    }

    public void setRegisterTime(long registerTime) {
        this.registerTime = registerTime;
    }

    public long getRegisterTime() {
        return registerTime;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
