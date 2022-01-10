package com.hiar.ar110.data;

/**
 * description : 所有字段值和海康平台配置信息保持一致
 * author : cuiqingchao
 * date : 2020/7/1 16:21
 */
public class PushConfig {
    String serverIp;//sip服务器ip
    int serverPort;//sip服务器端口

    String devicePort;//设备端配置端口号
    String deviceId;//设备号

    String sipId;//通话用id
    int kainterval;//心跳周期
    int expired;//重新注册间隔时间
    String authUser;//sip账号
    String authPassword;//sip密码
    String realm;//sip协议字段 域
}
