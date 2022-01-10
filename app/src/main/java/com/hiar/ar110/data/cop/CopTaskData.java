package com.hiar.ar110.data.cop;

import java.util.Arrays;

public class CopTaskData {
    public CopTaskRecord[] taskRecordList;
    public int from;      //起始编号
    public int limit;     //获取数量
    public String sort;   //增序减序排列
    public String order;  //排序方式
    public int total;

    @Override
    public String toString() {
        return "CopTaskData{" +
            "taskRecordList=" + Arrays.toString(taskRecordList) +
            ", from=" + from +
            ", limit=" + limit +
            ", sort='" + sort + '\'' +
            ", order='" + order + '\'' +
            ", total=" + total +
            '}';
    }
}
