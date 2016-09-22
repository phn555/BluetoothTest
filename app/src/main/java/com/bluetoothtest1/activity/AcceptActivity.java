package com.bluetoothtest1.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bluetoothtest1.bluetoothTools.BluetoothService;
import com.bluetoothtest1.mysql.SQLiteHelper;

import java.io.File;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * Created by Administrator on 2016/9/15.
 */
public class AcceptActivity extends Activity implements View.OnClickListener {


    //Debug检查

    private static final boolean D = true;
    public static final String TOAST = "toast";
    public static final String TAG = "BluetoothTest";
    public static final String DEVICE_NAME = "device_name";

    //按钮
    private Button scan, clear, accept, save;

    //
    private SQLiteHelper dbHelper;

    private SQLiteDatabase db;
    private ContentValues values;

    //数据显示布局 数据链表

    StringBuffer readData = new StringBuffer();
    //显示数据
    EditText listView;

    //连接状态显示
    private TextView connectDevices;
    private TextView issave, isaccept;


    private WritableWorkbook wwb;
    private String excelPath;
    private String name;
    private File excelFile;


    //连接蓝牙名称
    private String mConnectedDeviceName = null;

    private BluetoothService mCommService;  //蓝牙连接服务

    private BluetoothDevice device = null;//蓝牙设备

    private BluetoothAdapter bluetooth;//本地蓝牙适配器

    //bluetoothService 传来的消息状态
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    private int isSave = 1, isAccept = 1;
    //请求连接的requestCode
    static final int REQUEST_CONNECT_DEVICE = 2;
    // The Handler that gets information back from the BluetoothChatService
    //这个Handler用于获取BluetoothChatService返回的数据

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            mConnectedDeviceName = device.getName();
                            connectDevices.setText(R.string.title_connected_to);
                            connectDevices.append(mConnectedDeviceName);
                            //    mConversationArrayAdapter.clear();

                            break;
                        case BluetoothService.STATE_CONNECTING:
                            connectDevices.setText(R.string.title_connecting);
                            break;

                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            connectDevices.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    //  String writeMessage = new String(writeBuf);
                    //   mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;


                case MESSAGE_READ:

                    byte[] readBuf = (byte[]) msg.obj;

                    String read = new String(readBuf, 0, msg.arg1);
                    String get = "c";
                    String read2[] = read.split(get);
                    if(read2.length>1){
                        Log.i(TAG,"read2的长度："+read2.length);
                        for(int i=0;i<read2.length;i++){
                            Log.i(TAG,read2[i]);
                        }
                    }

                    String readMessage = read2[read2.length-1] ;
                    if (isAccept < 0) {
                        if(readMessage.toCharArray().length>1)
                        readData.append(readMessage + "\n");
                        listView.setText(readData);

                        if (isSave < 0) {
                            //添加到数据库
                            db = dbHelper.getWritableDatabase();
                            values = new ContentValues();
                            values.put("data", readMessage);
                            Long insert = db.insert("DateKu", null, values);

                            //保存到本地
                            if (insert > 0) {
                                Log.i(TAG, "insert =" + insert);
                                name = String.valueOf(insert);
                                writeToExcel(name, readMessage);
                            }
                            values.clear();
                        }
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.accept);
        //获取本地蓝牙
        bluetooth = BluetoothAdapter.getDefaultAdapter();
        //创建数据库
        dbHelper = new SQLiteHelper(this, "Date.db", null, 2);
        //创建保存路径
        excelPath = getExcelDir() + File.separator + "demo.xls";
        excelFile = new File(excelPath);
        createExcel(excelFile);

        connectDevices = (TextView) findViewById(R.id.connected_device);
        listView = (EditText) findViewById(R.id.listview);
        isaccept = (TextView) findViewById(R.id.accept1);
        issave = (TextView) findViewById(R.id.save1);

        //按键监听事件 scan ,clear ,accept ,save
        scan = (Button) findViewById(R.id.contentbnt);
        clear = (Button) findViewById(R.id.clearRx);
        accept = (Button) findViewById(R.id.accept_data);
        save = (Button) findViewById(R.id.saveData);

        scan.setOnClickListener(this);
        clear.setOnClickListener(this);
        accept.setOnClickListener(this);
        save.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //扫描
            case R.id.contentbnt:
                //搜索蓝牙
                Intent serverIntent = new Intent(AcceptActivity.this, SearchDerviceActivity.class);
                //启动带返回数据的活动
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                break;
            //清除
            case R.id.clearRx:
                listView.setText("");
                readData = new StringBuffer();
                break;
            //接收
            case R.id.accept_data:
                isAccept = ~isAccept;
                if (isAccept < 0) {
                    isaccept.setText("可以接收");
                } else {
                    isaccept.setText("不可接收");
                }
                break;
            //保存
            case R.id.saveData:

                isSave = ~isSave;
                if (isSave < 0) {
                    issave.setText("可以保存");
                } else {
                    issave.setText("不可保存");
                }

                break;
        }
    }


    //处理接受数据
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras().getString(SearchDerviceActivity.EXTRA_DEVICE_ADDRESS);
                    device = bluetooth.getRemoteDevice(address);
                    //Log.i(TAG, "获取到地址，准备连接" + address);
                    //尝试连接设备
                    mCommService.connect(device);
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //打开蓝牙请求
        if (!bluetooth.isEnabled()) {
            //请求打开蓝牙设备
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(intent);
        } else {
            // Toast.makeText(AcceptActivity.this, R.string.bluetooth_open_ok, Toast.LENGTH_SHORT).show();
            //  Log.i(TAG, "蓝牙打开");
            if (mCommService == null) {
                mCommService = new BluetoothService(this, mHandler);
            }

        }
    }

    @Override
    protected synchronized void onResume() {
        super.onResume();
        if (mCommService != null) {
            if (mCommService.getState() == BluetoothService.STATE_NONE) {
                // ，开启监听线程
                mCommService.start();
                Log.i(TAG, "开启监听线程");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mCommService != null) mCommService.stop();
        if (bluetooth != null) bluetooth.disable();

    }

    // 创建excel表.
    public void createExcel(File file) {
        WritableSheet ws = null;
        try {
            if (!file.exists()) {
                wwb = Workbook.createWorkbook(file);

                ws = wwb.createSheet("sheet1", 0);

                ws = wwb.createSheet("sheet2", 1);
                // 在指定单元格插入数据
                Label lbl1 = new Label(0, 0, "序号");
                Label bll2 = new Label(1, 0, "数据");

                ws.addCell(lbl1);
                ws.addCell(bll2);

                // 从内存中写入文件中
                wwb.write();
                wwb.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeToExcel(String name, String gender) {

        try {
            Workbook oldWwb = Workbook.getWorkbook(excelFile);
            wwb = Workbook.createWorkbook(excelFile,
                    oldWwb);
            WritableSheet ws = wwb.getSheet(0);
            // 当前行数
            int row = ws.getRows();
            Label lbl1 = new Label(0, row, name);
            Label bll2 = new Label(1, row, gender);

            ws.addCell(lbl1);
            ws.addCell(bll2);

            // 从内存中写入文件中,只能刷一次.
            wwb.write();
            wwb.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 获取Excel文件夹
    public String getExcelDir() {
        // SD卡指定文件夹
        String sdcardPath = Environment.getExternalStorageDirectory()
                .toString();
        File dir = new File(sdcardPath + File.separator + "Excel"
                + File.separator + "Person");
        if (dir.exists()) {
            return dir.toString();

        } else {
            dir.mkdirs();
            return dir.toString();
        }
    }
}
