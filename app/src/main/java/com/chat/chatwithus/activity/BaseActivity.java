package com.chat.chatwithus.activity;


import android.os.Bundle;
import android.app.Activity;
import com.chat.chatwithus.R;
import com.chat.chatwithus.net.NetThreadHelper;
import java.util.LinkedList;
import android.widget.Toast;
import java.io.IOException;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;

public abstract class BaseActivity extends Activity {

    protected static LinkedList<BaseActivity> queue = new LinkedList<BaseActivity>();
    private static MediaPlayer player;
    protected static NetThreadHelper netThreadHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        netThreadHelper = NetThreadHelper.newInstance();


        if(!queue.contains(this))
            queue.add(this);
        if(player == null){
            player = MediaPlayer.create(this, R.raw.msg);
            try {
                player.prepare();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    public void makeTextShort(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }


    public abstract void processMessage(Message msg);

    @Override
    public void finish() {
        // TODO Auto-generated method stub
        super.finish();
        queue.removeLast();
    }

    public static void sendMessage(Message msg) {
        handler.sendMessage(msg);
    }

    public static void sendEmptyMessage(int what) {
        handler.sendEmptyMessage(what);
    }

    private static Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            if(queue.size() > 0)
                queue.getLast().processMessage(msg);

        }

    };

    public void exit() {
        while (queue.size() > 0)
            queue.getLast().finish();
    }

    public static void playMsg(){
        player.start();
    }

}