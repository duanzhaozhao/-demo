package com.example.skysoft.decordemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    BluetoothAdapter mBluetoothAdapter;
    List<String> mArrayAdapter = new ArrayList<String>();
    List<String> mArrayAdapter1 = new ArrayList<String>();
    ListView list, list1;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    ArrayAdapter arrayAdapter1 = new ArrayAdapter(MainActivity.this, android.R.layout.simple_expandable_list_item_1, mArrayAdapter1);
                    arrayAdapter1.notifyDataSetChanged();
                    list1.setAdapter(arrayAdapter1);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = (ListView) findViewById(R.id.list);
        list1 = (ListView) findViewById(R.id.list1);
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
//1.
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//代表自己设备的蓝牙适配器
        if (mBluetoothAdapter == null) {//判断支不支持蓝牙设备
            Toast.makeText(this, "此设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            finish();
        }
//2.开启蓝牙
        if (!mBluetoothAdapter.isEnabled()) {//判断是否打开蓝牙设备
            //请求开启蓝牙
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
//服务端必须加这个
        Intent displayIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        displayIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivity(displayIntent);
// 3.       获得已配对的蓝牙设备
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
// If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                Log.i("aaa", "已配对的设备" + device.getName());
            }

        } else {
            Log.i("aaa", "搜索蓝牙");
            mBluetoothAdapter.startDiscovery();//搜索蓝牙
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, mArrayAdapter);
        list.setAdapter(arrayAdapter);
        new AcceptThread().start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            switch (resultCode) {
                case RESULT_OK:
                    Toast.makeText(this, "打开蓝牙成功", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("", UUID.fromString("4117fffb-c411-46db-82e4-f94a2e9a5f10"));
            } catch (IOException e) {
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
//                    必须延迟一下
                    Thread.sleep(2000);
                } catch (Exception e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    // manageConnectedSocket(socket);

                    try {


                        Log.i("aaa", "发送数据");
                        OutputStream out = socket.getOutputStream();
                        byte[] by ;
                        StringBuilder s = new StringBuilder();
                        for(int i = 0;i<900;i++) {
                            s.append("a");
                        }
                      String a=  s.toString();
                       Log.i("aaa",s.toString());
                       Log.i("aaa1", String.valueOf(s.length()));
                        by = a.getBytes();

                        by = new byte[8192];
                        for(int i=0; i<8192;i+=900)
                        {//OutputStream一次不能写太多 分几次发送
                            int b = ((i+900) < 8192) ? 900: 8192 - i;
                            out.write(by,i,b);
                            out.flush();
                            Log.i("aaa", String.valueOf(by.length));
                        }
//                         out.write(by);
                        Log.i("aaa", "发送数据1");
//                        mmServerSocket.close();
//                        cancel();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        /**
         * Will cancel the listening socket, and cause the thread to finish
         */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
            }
        }
    }

    //扫描设备
    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter1.add(device.getName() + "\n" + device.getAddress());
                Message message = new Message();
                handler.sendEmptyMessage(0);
//                if (device.getName() != null) {
//                    if (device.getName().equals("小米手机6")) {
//                        mBluetoothAdapter.cancelDiscovery();
//                        Log.i("aaa", "搜索到");
//                    }
//                }
            }
        }
    };

}
