package com.example.client;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public BluetoothAdapter mBluetoothAdapter;
    List<String> mArrayAdapter = new ArrayList<String>();
    List<String> mArrayAdapter1 = new ArrayList<String>();
    List<String> mArrayAdapter2 = new ArrayList<String>();
    List<String> mArrayAdapter3 = new ArrayList<String>();
    ListView list, list1;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    ArrayAdapter arrayAdapter1 = new ArrayAdapter(MainActivity.this,
                            android.R.layout.simple_expandable_list_item_1, mArrayAdapter1);
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

// 3.       获得已配对的蓝牙设备
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
// If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mArrayAdapter3.add(device.getAddress());
                Log.i("aaa", "已配对的设备" + device.getName());
            }

        } else {
            Log.i("aaa", "搜索蓝牙");
            mBluetoothAdapter.startDiscovery();//搜索蓝牙
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, mArrayAdapter);
        list.setAdapter(arrayAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(mArrayAdapter3.get(position));
                Log.i("aaa", mArrayAdapter3.get(position));
                new ConnectThread(bluetoothDevice).start();
                Log.i("aaa", "点击item1");
            }
        });

        list1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(mArrayAdapter2.get(position));

                new ConnectThread(bluetoothDevice).start();
                Log.i("aaa", "===========" + mArrayAdapter2.get(position));
                Log.i("aaa", "点击item");
            }
        });

    }


    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("4117fffb-c411-46db-82e4-f94a2e9a5f10"));
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                Log.i("aaa", "开始连接设备");
                mmSocket.connect();
                try {
//                    必须延迟一下
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (mmSocket != null) {
                    byte[] buffer = new byte[1024];  // buffer store for the stream
                    int bytes;
                    InputStream mmInStream = mmSocket.getInputStream();
//                    bytes = mmInStream.read();//没有执行下面
                    bytes = mmInStream.read(buffer);
                    if (bytes != 0) {
                        String value = new String(buffer);

                        System.out.println(value.trim().toString());
                        Log.i("aaa", value.trim().toString() + "a");
                    } else {
                        Log.i("aaa", "无数据");
                    }

                } else {
                    Log.i("aaa", "4");
                }
//                new ConnectedThread(mmSocket).start();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
//                try {
//                    Log.i("aaa", "连接失败"+connectException.getMessage());
//                    mmSocket.close();
//                } catch (IOException closeException) {
//                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
//            manageConnectedSocket(mmSocket);
        }

        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            try {
                mmSocket.close();
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
                mArrayAdapter1.add(device.getName() + "\n" + device.getAddress());
                mArrayAdapter2.add(device.getAddress());
                Message message = new Message();
                handler.sendEmptyMessage(0);
//                if (device.getName() != null) {
//                    if (device.getName().equals("OPPO R9tmduan")) {
//                        Log.i("aaa", "搜索到");
//                    }
//                }
            }
        }


    };


    //    发送消息
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
//                bytes = mmInStream.read(buffer);//没有执行下面
                    bytes = mmInStream.read();//没有执行下面

                    if (bytes != 0) {
                        Log.i("aaa", buffer.toString() + "a");
                    }
                } catch (IOException e) {
                    Log.i("aaa", e.getMessage());
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }
}
