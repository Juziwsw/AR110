package com.example.mediaengine.entity;

import java.util.List;

/**
 * description : TODO:类的作用
 * author : cuiqingchao
 * date : 2020/2/25 11:13
 */
public class EngineConfigInfo {
    String appKey;
    String appSecret;
    String serverAddress;
    int serverPort;
    int serverAddressCount;
    List<String> serverAddressList;
    MediaType mediaType;


    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getServerAddressCount() {
        return serverAddressCount;
    }

    public void setServerAddressCount(int serverAddressCount) {
        this.serverAddressCount = serverAddressCount;
    }

    public List<String> getServerAddressList() {
        return serverAddressList;
    }

    public void setServerAddressList(List<String> serverAddressList) {
        this.serverAddressList = serverAddressList;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }
}
