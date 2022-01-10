package com.hiscene.imui.bean;

public enum MsgSendStatus {
    CHAT_MSG_STATUS_NONE ,   			//无效状态
    CHAT_MSG_STATUS_SENDING,   			//发送中
    CHAT_MSG_STATUS_SENT , 				//发送完成
    CHAT_MSG_STATUS_SEND_FAILED , 		//发送失败
    CHAT_MSG_STATUS_WAITING ,			//等待接收
    CHAT_MSG_STATUS_RECEIVING , 		//接收中
    CHAT_MSG_STATUS_RECEIVED , 			//接收完成
    CHAT_MSG_STATUS_RECEIVE_FAILED , 	//接收失败
}
