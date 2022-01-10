package com.example.mediaengine.entity.im;


public final class AccountState {
    private int stateCode;
    private String hintContent;

    public final int getStateCode() {
        return this.stateCode;
    }

    public final void setStateCode(int var1) {
        this.stateCode = var1;
    }

    public final String getHintContent() {
        return this.hintContent;
    }

    public final void setHintContent(String var1) {
        this.hintContent = var1;
    }
}