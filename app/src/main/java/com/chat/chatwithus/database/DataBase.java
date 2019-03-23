package com.chat.chatwithus.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBase extends SQLiteOpenHelper {

    private Context mcontext;

    public static final String CREATE_INFO = "create table Info ("+" ip text,"+"name text,"+"msg,text)";
    public DataBase(Context context,String name,SQLiteDatabase.CursorFactory factory,int version){
          super(context,name,factory,version);
          mcontext=context;
    }
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_INFO);
    }

    public void onUpgrade(SQLiteDatabase db,int oldvVersion,int newVersion){

    }
}
