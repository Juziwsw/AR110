package com.example.mediaengine.entity;

public class ActionResult {

    public enum ACTION_TYPE{
        MARK, FREEZE, BOARD, FOCUS, ZOOM, REMOTE_CONTROL
    }
    private ACTION_TYPE type;
    private String description;

    public ACTION_TYPE getType() {
        return type;
    }

    public void setType(ACTION_TYPE type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
