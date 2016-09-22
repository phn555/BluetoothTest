package com.bluetoothtest1.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bluetoothtest1.bluetoothTools.BluetoothService;


public class MainActivity extends Activity implements View.OnClickListener {

    private Context mContext;
    private Button discoverable, chat, accept, about, exit;
    private TextView is_Discoverable ;

    BluetoothAdapter bluetooth ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mContext = this;
        //获取本地蓝牙
        bluetooth = BluetoothAdapter.getDefaultAdapter();

        discoverable = (Button) findViewById(R.id.discoverable); //蓝牙可见性
        chat = (Button) findViewById(R.id.chat);                  //聊天模式
        accept = (Button) findViewById(R.id.accept);               //接收模式
        about = (Button) findViewById(R.id.about);                 //关于
        exit = (Button) findViewById(R.id.exit);                    //退出
        is_Discoverable = (TextView) findViewById(R.id.is_Discoverable);

        discoverable.setOnClickListener(this);
        chat.setOnClickListener(this);
        accept.setOnClickListener(this);
        about.setOnClickListener(this);
        exit.setOnClickListener(this);



    }

    @Override
    public void onClick(View v) {
        //按键监听事件
        switch (v.getId()) {
            case R.id.discoverable:
                try {
                    openDiscoverable();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.chat:
                Toast.makeText(MainActivity.this, "对不起，暂无此功能", Toast.LENGTH_SHORT).show();
                break;
            case R.id.accept:
                Intent intent1 = new Intent(MainActivity.this, AcceptActivity.class);
                startActivity(intent1);
                break;
            case R.id.about:
                Intent intent2 = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent2);
                break;
            case R.id.exit:
                Toast.makeText(MainActivity.this, R.string.see_you, Toast.LENGTH_LONG).show();
                finish();
                break;
        }
    }

    private void openDiscoverable() {

        //判断蓝牙是否开启
        if (!bluetooth.isEnabled()) {
            //请求打开蓝牙设备
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(intent);
        } else {
            Toast.makeText(MainActivity.this, R.string.bluetooth_open_ok, Toast.LENGTH_SHORT).show();
        }
        //设置蓝牙可见时间
        if (bluetooth.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {

            Intent displayIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            //设置最长蓝牙可见时间
            displayIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 10);
            startActivity(displayIntent);
        } else {
            is_Discoverable.setText("开");
            Toast.makeText(MainActivity.this, R.string.bluetooth_discoverable, Toast.LENGTH_SHORT).show();
        }
    }

}
