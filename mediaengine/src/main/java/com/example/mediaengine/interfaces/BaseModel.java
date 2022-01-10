package com.example.mediaengine.interfaces;

/**
 * Created by hujun on 18/8/29.
 */

public interface BaseModel<U,C> {
    long transferUserId(U t);
    U transferUserId(long userId);

    long transferChannelId(C c);
    C transferChannelId(long channelId);
}
