package com.hiar.ar110.data.cop;

public class CopTaskReqData {
    public int id;
    public String cjdbh;     //处警单编号
    public String cjdbh2;    //出警单编号
    public String jybh;      //警员编号
    public String jyxm;      //出警员姓名
    public String jyxmMatch; //出警员姓名（模糊查询)
    public int    cjzt;      //出警状态
    public int from;         //起始编号
    public int limit ;       //长度
    public String order;     //不传默认为：id, 现有排序字段：id, bjsj(报警时间),cjdbh（处警单编号）
    public String sort;      //排序逻辑
}

