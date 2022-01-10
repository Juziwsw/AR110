package com.example.mediaengine.engines.agoraengine;

import com.example.mediaengine.interfaces.BaseModel;

/**
 * Created by hujun on 2018/11/6.
 */

public class AgoraModel implements BaseModel<Integer,String> {
    @Override
    public long transferUserId(Integer s) {
        return s;
    }

    @Override
    public Integer transferUserId(long userId) {
        return (int)userId;
    }
    @Override
    public long transferChannelId(String s) {
        return Long.parseLong(s);
    }

    @Override
    public String transferChannelId(long channelId) {
        return String.valueOf(channelId);
    }
}
