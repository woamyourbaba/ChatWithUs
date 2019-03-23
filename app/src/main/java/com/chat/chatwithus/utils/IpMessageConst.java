package com.chat.chatwithus.utils;



public class IpMessageConst {
    public static final int VERSION = 0x001;		// 版本号
    public static final int PORT = 0x0979;			// 端口号，协议默认端口2425

    // 命令
    public static final int IPMSG_BR_ENTRY			 = 0x00000001;	//用户上线
    public static final int IPMSG_BR_EXIT		 	 = 0x00000002;	//用户退出
    public static final int IPMSG_ANSENTRY			 = 0x00000003;	//通报在线
    public static final int IPMSG_SENDMSG 			 = 0x00000020;	//发送消息
    public static final int IPMSG_RECVMSG 			 = 0x00000021;	//通报收到消息
    public static final int IPMSG_RELEASEFILES		 = 0x00000061;	//丢弃附加文件
    public static final int IPMSG_FILEATTACHOPT 	 = 0x00200000;	//附加文件
    public static final int IPMSG_SENDCHECKOPT = 0x00000100;	//传送验证





}