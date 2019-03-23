package com.chat.chatwithus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.chat.chatwithus.R;
import com.chat.chatwithus.activity.UserListActivity;
import com.chat.chatwithus.utils.UsedConst;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.btnsure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edt=(EditText)findViewById(R.id.ed);
                String str=edt.getText().toString();
                if(str.length()!=0)
                    UsedConst.name=str;
                startActivity(new Intent(MainActivity.this, UserListActivity.class));
            }
        });

    }


}
