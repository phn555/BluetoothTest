package com.bluetoothtest1.mysql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by Administrator on 2016/9/18.
 */
public class SQLiteHelper extends SQLiteOpenHelper {
    public static final String CREATE_DATAKU = "create table DateKu("
            + " id integer primary key autoincrement ,"
            +"data text) ";

    private Context mContext ;

    public SQLiteHelper(Context context , String name ,
                        SQLiteDatabase.CursorFactory factory ,int version){
        super(context ,name ,factory ,version) ;
        mContext = context ;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DATAKU);
        Toast.makeText(mContext ,"Create succeeded" ,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table id exists DateKu");
    }
}
