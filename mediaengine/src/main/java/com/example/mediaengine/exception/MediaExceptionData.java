package com.example.mediaengine.exception;

/**
 *
 * @author hujun
 * @date 18/8/27
 * +
 */

public class MediaExceptionData extends Throwable {
    public final int errorCode;

    public MediaExceptionData(int errorCode) {
        this.errorCode = errorCode;
    }

    public MediaExceptionData(int errorCode, String s) {
        super(s);
        this.errorCode = errorCode;
    }
}