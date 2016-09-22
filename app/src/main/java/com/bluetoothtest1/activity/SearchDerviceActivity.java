package com.bluetoothtest1.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by Administrator on 2016/9/15.
 */
public class SearchDerviceActivity extends Activity {

    //Debug检查
    public static final String TAG = "BluetoothTest";
    private static final boolean D = true;
    //Intent 返回的地址
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    //是否搜索到新设备的标志 0:有 ； 1 ：没有
    private int deviceFlag = 0;

    private BluetoothAdapter bluetooth;

    private ListView newDevicesList;
    private ListView pairedDevicesList;
    private ArrayAdapter<String> pairedDevicesAdapter;
    private ArrayAdapter<String> newDevicesAdapter;

    private ArrayList<String> newDevices = new ArrayList<String>();
    private Button scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);

        bluetooth = BluetoothAdapter.getDefaultAdapter();
        //从paired devices获取一个set
        Set<BluetoothDevice> pairedDevices = bluetooth.getBondedDevices();

        scanButton = (Button) findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.scanButton) {
                    doDiscovery();
                    v.setVisibility(View.GONE);
                }

            }
        });

        pairedDevicesList = (ListView) findViewById(R.id.pairedDevice);
        pairedDevicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        pairedDevicesList.setAdapter(pairedDevicesAdapter);
        pairedDevicesList.setOnItemClickListener(pairedDeviceClickListener);

        newDevicesList = (ListView) findViewById(R.id.newDevice);
        newDevicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, newDevices);
        newDevicesList.setAdapter(newDevicesAdapter);
        newDevicesList.setOnItemClickListener(newDeviceClickListener);


        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);


        if (pairedDevices.size() > 0) {

            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.noPairedDevice).toString();

            pairedDevicesAdapter.add(noDevices);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Make sure we're not doing discovery anymore
        if (bluetooth != null) {
            bluetooth.cancelDiscovery();
        }
        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    private void doDiscovery() {

        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scaning);
        if (bluetooth.isDiscovering()) {
            bluetooth.cancelDiscovery();
        }
        bluetooth.startDiscovery();


    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //当发现设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                deviceFlag = 0;
                //如果已经配对，则忽略
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    newDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                setProgressBarIndeterminateVisibility(false);

                if (newDevicesAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.noNewDevice).toString();
                    newDevicesAdapter.add(noDevices);
                    deviceFlag = 1;
                } else {
                    deviceFlag = 0;

                }

                if (D) Log.d(TAG, "搜索完成noDeviceFlag=" + deviceFlag);
            }
            if(!newDevicesAdapter.equals(R.string.noNewDevice) || pairedDevicesAdapter.equals(R.string.noNewDevice) ){
                setTitle(R.string.select_device);
            }
        }
    };

    private AdapterView.OnItemClickListener newDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            //当选择连接之后，关闭搜索过程，此过程消耗比较大
            bluetooth.cancelDiscovery();

            // 从点击的Item中获得Mac地址，由于Mac地址格式可以其为最后的17位
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            if (D) Log.d(TAG, "start to connecting" + address);
            finish();
        }
    };

    private AdapterView.OnItemClickListener pairedDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            //当选择连接之后，关闭搜索过程，此过程消耗比较大
            bluetooth.cancelDiscovery();

            // 从点击的Item中获得Mac地址，由于Mac地址格式可以其为最后的17位
            //添加noDeviceFlag标志，防止在没有搜索到设备时仍然选择连接而造成程序中断的bug
            if (deviceFlag == 0) {
                String info = ((TextView) v).getText().toString();
                String address = info.substring(info.length() - 17);
                // Create the result Intent and include the MAC address
                Intent intent = new Intent();
                intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

                // Set result and finish this Activity
                setResult(Activity.RESULT_OK, intent);
            }
            finish();
        }
    };
}
