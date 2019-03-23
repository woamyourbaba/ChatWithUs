package com.chat.chatwithus.activity;


import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


import android.content.Intent;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.chat.chatwithus.R;
import com.chat.chatwithus.adapter.ChatListAdapter;
import com.chat.chatwithus.data.ChatMessage;
import com.chat.chatwithus.interfaces.ReceiveMsgListener;
import com.chat.chatwithus.utils.IpMessageConst;
import com.chat.chatwithus.utils.IpMessageProtocol;
import com.chat.chatwithus.utils.UsedConst;
import com.chat.chatwithus.database.DataBase;


public class ChatActivity extends BaseActivity implements OnClickListener,ReceiveMsgListener {
    private TextView chat_name;
    private TextView chat_mood;
    private Button chat_quit;
    private ListView chat_list;
    private EditText chat_input;
    private Button chat_send;
    private ImageView chat_item_head;

    private List<ChatMessage> msgList;
    private String receiverName;
    private String receiverIp;
    private String receiverGroup;
    private ChatListAdapter adapter;
    private String selfName;
    private String selfGroup;
    private DataBase db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        db=new DataBase(this,"Message.db",null,1);
        setContentView(R.layout.activity_chat);
        Intent intent1=getIntent();
        int j=intent1.getIntExtra("image",0);
        findViews();
        int [] imgIds={R.drawable.user1,R.drawable.user2,R.drawable.user3,R.drawable.user4,R.drawable.user5};
        chat_item_head.setImageResource(imgIds[j]);
        msgList = new ArrayList<ChatMessage>();
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        receiverName = bundle.getString("receiverName");
        receiverIp = bundle.getString("receiverIp");
        receiverGroup = bundle.getString("receiverGroup");
        selfName = UsedConst.name;
        selfGroup = "android";

        chat_name.setText(receiverName + "(" + receiverIp + ")");
        chat_mood.setText("guorp name：" + receiverGroup);
        chat_quit.setOnClickListener(this);
        chat_send.setOnClickListener(this);

        Iterator<ChatMessage> it = netThreadHelper.getReceiveMsgQueue().iterator();
        while(it.hasNext()){	//循环消息队列，获取队列中与本聊天activity相关信息
            ChatMessage temp = it.next();

            if(receiverIp.equals(temp.getSenderIp())){
                msgList.add(temp);	//添加到显示list
                it.remove();		//将本消息从消息队列中移除
            }
        }

        adapter = new ChatListAdapter(this, msgList);
        chat_list.setAdapter(adapter);

        netThreadHelper.addReceiveMsgListener(this);	//注册到listeners
    }

    private void findViews(){

        chat_name = (TextView) findViewById(R.id.chat_name);
        chat_mood = (TextView) findViewById(R.id.chat_mood);
        chat_quit = (Button) findViewById(R.id.chat_quit);
        chat_list = (ListView) findViewById(R.id.chat_list);
        chat_input = (EditText) findViewById(R.id.chat_input);
        chat_send = (Button) findViewById(R.id.chat_send);
        chat_item_head=(ImageView)findViewById(R.id.chat_item_head);

    }

    @Override
    public void processMessage(Message msg) {
        // TODO Auto-generated method stub
        switch(msg.what){
            case IpMessageConst.IPMSG_SENDMSG:
                adapter.notifyDataSetChanged();	//刷新ListView
                break;


        }
    }

    @Override
    public boolean receive(ChatMessage msg) {
        // TODO Auto-generated method stub
        if(receiverIp.equals(msg.getSenderIp())){	//若消息与本activity有关，则接收
            msgList.add(msg);	//将此消息添加到显示list中
            sendEmptyMessage(IpMessageConst.IPMSG_SENDMSG); //使用handle通知，来更新UI
            playMsg();
            return true;
        }

        return false;
    }

    @Override
    public void finish() {
        // TODO Auto-generated method stub
        netThreadHelper.removeReceiveMsgListener(this);
        super.finish();
    }

    @Override
    public void onClick(View v) {

        // TODO Auto-generated method stub
        if(v == chat_send){
            db.getWritableDatabase();
            sendAndAddMessage();
        }else if(v == chat_quit){
            SQLiteDatabase dbhelp=db.getWritableDatabase();
            Cursor cursor=dbhelp.query("Info",null,null,null,null,null,null);
            if(cursor.moveToFirst()){
                String ip=cursor.getString(cursor.getColumnIndex("ip"));
                if(ip!=receiverIp)
                {
                    ContentValues cv=new ContentValues();
                    cv.put("ip",receiverIp);
                    cv.put("name",receiverName);
                    cv.put("msg",msgList.toString());
                    dbhelp.insert("Info",null,cv);
                }else {

                }
            }

            finish();
        }
    }

    /**
     * 发送消息并将该消息添加到UI显示
     */
    private void sendAndAddMessage(){
        String msgStr = chat_input.getText().toString().trim();
        if(!"".equals(msgStr)){
            //发送消息
            final IpMessageProtocol sendMsg = new IpMessageProtocol();
            sendMsg.setVersion(String.valueOf(IpMessageConst.VERSION));
            sendMsg.setSenderName(selfName);
            sendMsg.setSenderHost(selfGroup);
            sendMsg.setCommandNo(IpMessageConst.IPMSG_SENDMSG);
            sendMsg.setAdditionalSection(msgStr);

            try {
                final InetAddress sendto = InetAddress.getByName(receiverIp);
                if(sendto != null){
                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            netThreadHelper.sendUdpData(sendMsg.getProtocolString() + "\0", sendto, IpMessageConst.PORT);
                        }
                    } ).start();
                }
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block

            }



            //添加消息到显示list
            ChatMessage selfMsg = new ChatMessage("localhost", selfName, msgStr, new Date());
            selfMsg.setSelfMsg(true);	//设置为自身消息
            msgList.add(selfMsg);

        }else{
            makeTextShort("content is null");
        }

        chat_input.setText("");
        adapter.notifyDataSetChanged();//更新UI
    }
}
