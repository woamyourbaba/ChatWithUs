package com.chat.chatwithus.activity;


import android.os.Bundle;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import com.chat.chatwithus.data.User;
import com.chat.chatwithus.database.DataBase;
import com.chat.chatwithus.net.NetThreadHelper;
import com.chat.chatwithus.utils.IpMessageConst;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import android.widget.ExpandableListView;
import android.widget.TextView;
import com.chat.chatwithus.R;
import com.chat.chatwithus.adapter.UserExpandableListAdapter;
import com.chat.chatwithus.data.ChatMessage;




public class UserListActivity extends BaseActivity implements OnClickListener {
    public static String hostIp;
    private ExpandableListView userList;
    private UserExpandableListAdapter adapter;
    private List<String> strGroups; //所有一级菜单名称集合
    private List<List<User>> children;
    private TextView totalUser;
    private Button refreshButton;
    private TextView ipTextView;

    Context context;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usrlist);

        findViews();
        refreshButton.setOnClickListener(this);
        context = this;


        strGroups = new ArrayList<String>(); //所有一级菜单名称集合
        children = new ArrayList<List<User>>();

        netThreadHelper = NetThreadHelper.newInstance();
        netThreadHelper.connectSocket();	//开始监听数据
        new Thread( new Runnable() {
            @Override
            public void run() {
                netThreadHelper.noticeOnline();	//广播上线
            }
        } ).start();


        adapter = new UserExpandableListAdapter(this, strGroups, children);
        userList.setAdapter(adapter);

        refreshViews();
    }


    @Override
    public void finish() {
        // TODO Auto-generated method stub
        super.finish();
        netThreadHelper.noticeOffline();	//通知下线
        netThreadHelper.disconnectSocket(); //停止监听

    }


    private void findViews() {
        // TODO Auto-generated method stub
        totalUser =(TextView) findViewById(R.id.totalUser);
        userList = (ExpandableListView) findViewById(R.id.userlist);
        refreshButton = (Button) findViewById(R.id.refresh);;
        ipTextView = (TextView) findViewById(R.id.mymood);
        hostIp = getLocalIpAddress();
        ipTextView.setText(hostIp);	//设置IP
    }


    @Override
    public void processMessage(Message msg) {
        // TODO Auto-generated method stub
        switch(msg.what){
            case IpMessageConst.IPMSG_BR_ENTRY:
            case IpMessageConst.IPMSG_BR_EXIT:
            case IpMessageConst.IPMSG_ANSENTRY:
            case IpMessageConst.IPMSG_SENDMSG:
                refreshViews();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK)
            exit();
        return true;
    }

    //更新数据和UI显示
    private void refreshViews(){
        //清空数据
        strGroups.clear();
        children.clear();

        Map<String,User> currentUsers = new HashMap<String, User>();
        currentUsers.putAll(netThreadHelper.getUsers());
        Queue<ChatMessage> msgQueue = netThreadHelper.getReceiveMsgQueue();
        Map<String, Integer> ip2Msg = new HashMap<String, Integer>();	//IP地址与未收消息个数的map
        //遍历消息队列，填充ip2Msg
        Iterator<ChatMessage> it = msgQueue.iterator();
        while(it.hasNext()){
            ChatMessage chatMsg = it.next();
            String ip = chatMsg.getSenderIp();	//得到消息发送者IP
            Integer tempInt = ip2Msg.get(ip);
            if(tempInt == null){	//若map中没有IP对应的消息个数,则把IP添加进去,值为1
                ip2Msg.put(ip, 1);
            }else{	//若已经有对应ip，则将其值加一
                ip2Msg.put(ip, ip2Msg.get(ip)+1);
            }
        }

        //遍历currentUsers,更新strGroups和children
        Iterator<String> iterator = currentUsers.keySet().iterator();
        while (iterator.hasNext()) {
            User user = currentUsers.get(iterator.next());
            //设置每个在线用户对应的未收消息个数
            if(ip2Msg.get(user.getIp()) == null){
                user.setMsgCount(0);
            }else{
                user.setMsgCount(ip2Msg.get(user.getIp()));
            }

            String groupName = user.getGroupName();
            int index = strGroups.indexOf(groupName);
            if(index == -1){ //没有相应分组，则添加分组，并添加对应child
                strGroups.add(groupName);


                List<User> childData = new ArrayList<User>();
                childData.add(user);
                children.add(childData);
            }else{	//已存在分组，则将对应child添加到相对应分组中

                children.get(index).add(user);
            }

        }




        adapter.notifyDataSetChanged();	//更新ExpandableListView

        String countStr = "number of online:" + currentUsers.size() ;
        totalUser.setText(countStr);	//更新TextView

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if(v.equals(refreshButton)){	//若是刷新
            new Thread( new Runnable() {
                @Override
                public void run() {
                    netThreadHelper.noticeOnline();	//广播上线
                }
            } ).start();

            refreshViews();
        }

    }

    //判断wifi是否打开
    public boolean isWifiActive(){
        ConnectivityManager mConnectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(mConnectivity != null){
            NetworkInfo[] infos = mConnectivity.getAllNetworkInfo();

            if(infos != null){
                for(NetworkInfo ni: infos){
                    if("WIFI".equals(ni.getTypeName()) && ni.isConnected())
                        return true;
                }
            }
        }
        return false;
    }

    //得到本机IP地址
    public String getLocalIpAddress(){
        try{
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while(en.hasMoreElements()){
                NetworkInterface nif = en.nextElement();
                Enumeration<InetAddress> enumIpAddr = nif.getInetAddresses();
                while(enumIpAddr.hasMoreElements()){
                    InetAddress mInetAddress = enumIpAddr.nextElement();
                    if(!mInetAddress.isLoopbackAddress() && !mInetAddress.isLinkLocalAddress()){
                        return mInetAddress.getHostAddress().toString();
                    }
                }
            }
        }catch(SocketException ex){

        }

        return null;
    }

    //获取本机MAC地址
    public String getLocalMacAddress(){
        WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }
}