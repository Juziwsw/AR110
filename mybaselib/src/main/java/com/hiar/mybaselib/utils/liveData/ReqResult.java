package com.hiar.mybaselib.utils.liveData;

/**
 * description : 公共模块接口返回结果
 * author : cuiqingchao
 * date : 2019/9/20 10:29
 */
public class ReqResult<T> {
    private int status = 0;         //0为失败 1为成功
    private T data = null;         //成功时返回的数据体,如果没有返回此数据体T即为String填充返回成功的自定义提示信息
    private String errorMsg = "";   //失败时返回的信息

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}