package com.chat.chatwithus.data;


import java.text.SimpleDateFormat;
import java.util.Date;


public class ChatMessage {
    private String senderIp;	//消息发送者的ip
    private String senderName;	//消息发送者的名字
    private String msg;			//信息内容
    private Date time;		//发送时间 :格式：
    private boolean selfMsg;	//是否自己发送


    public ChatMessage(String senderIp, String senderName,
                       String msg, Date time) {
        super();
        this.senderIp = senderIp;
        this.senderName = senderName;
        this.msg = msg;
        this.time = time;
        this.selfMsg = false;	//默认不是自己
    }
    public String getSenderIp() {
        return senderIp;
    }

    public String getSenderName() {
        return senderName;
    }


    public String getMsg() {
        return msg;
    }

    public boolean isSelfMsg() {
        return selfMsg;
    }
    public void setSelfMsg(boolean selfMsg) {
        this.selfMsg = selfMsg;
    }

    public String getTimeStr(){	//返回格式为HH:mm:ss的时间字符串
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        return sdf.format(time);
    }
}