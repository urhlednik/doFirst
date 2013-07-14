/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.samsung.ble.anpserver;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements Button.OnClickListener {

    String TAG = "MainActivity";

    int alert_value = 0;
    int alert_level = 1;
    Context mContext = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    ANPServerService mService;
    Button mBtnSelectDevice, mBtndisconnect, mSendEvent;
    TextView mTVDeviceName;
    SoundManager SM;
    private static final int STATE_NONE = 0;
    private static final int STATE_CONNECTED = 1;
    private static final int STATE_DISCONNECTED = 2;
    private int mState = STATE_NONE;
    private BluetoothDevice mDevice = null;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_SELECT_DEVICE = 2;
    private static final int STATE_OFF = 10;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case ANPServerService.ANS_CONNECT_MSG:
                Bundle data = msg.getData();
                final BluetoothDevice device = data.getParcelable(BluetoothDevice.EXTRA_DEVICE);
                mDevice = device;
                Log.d(TAG, "ANS_CONNECT_MSG");
                runOnUiThread(new Runnable() {
                    public void run() {
                        mState = STATE_CONNECTED;
                        setUiState();
                    }
                });
                break;
            case ANPServerService.ANS_DISCONNECT_MSG:
                Log.d(TAG, "ANS_DISCONNECT_MSG");
                runOnUiThread(new Runnable() {
                    public void run() {
                        mState = STATE_DISCONNECTED;
                        setUiState();
                    }
                });
                break;
            default:
                super.handleMessage(msg);
            }
        }
    };
    private final BroadcastReceiver ANSChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = mIntent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int devState = mIntent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                Log.d(TAG, "BluetoothDevice.ACTION_BOND_STATE_CHANGED");
                setUiState();
                if (device.equals(mDevice) && devState == BluetoothDevice.BOND_NONE) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mDevice = null;
                            setUiState();
                        }
                    });
                }
            }
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = mIntent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);
                Log.d(TAG, "BluetoothAdapter.ACTION_STATE_CHANGED" + "state is" + state);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if ( state == STATE_OFF) {
                                mState = STATE_DISCONNECTED;
                                setUiStateForBTOff();
                            }
                        }
                    });
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("MainActivity", "OnCreate");
        setContentView(R.layout.main1);
        mContext = getApplicationContext();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        init();
        Intent intent = getIntent();
        if (intent != null) {
            alert_level = intent.getIntExtra("alert_level", 0);
        }
        if (alert_level == 0) {
            if (SM != null) {
                Log.e("MainActivity", "stop sound");
                SM.stopSound();
                SM.releaseSound();
                finish();
            }
        }

        if (SM == null) {
            Log.e("MainActivity", "play sound ");
            SM = SoundManager.getInstance();
            SM.initSounds(this);
            SM.addSound(alert_level);
        }
        mBtnSelectDevice = (Button) findViewById(R.id.btn_select);
        mBtnSelectDevice.setOnClickListener(this);

        mBtndisconnect = (Button) findViewById(R.id.btn_disconnect);
        mBtndisconnect.setEnabled(false);
        mBtndisconnect.setOnClickListener(this);

        mSendEvent = (Button) findViewById(R.id.button_send_alert);
        mSendEvent.setOnClickListener(this);
        mSendEvent.setEnabled(false);

        ((Button) findViewById(R.id.btn_remove_bond)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.removeBond(mDevice);
            }
        });

        mTVDeviceName = (TextView) findViewById(R.id.deviceName);
        setUiState();
    }

    private void init() {
        Log.d(TAG, "init() mService= " + mService);
        Intent bindIntent = new Intent(this, ANPServerService.class);
        startService(bindIntent);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(ANSChangeReceiver, filter);
    }

    ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName arg0) {
            Log.i(TAG, "MainActivity::onServiceDisconnected");
            mService = null;
            Toast.makeText(mContext, "ANS Service disconnected", Toast.LENGTH_LONG).show();
            finish();
        }

        public void onServiceConnected(ComponentName arg0, IBinder arg1) {

            Log.i(TAG, "MainActivity::onServiceConnected");
            mService = ((ANPServerService.LocalBinder) arg1).getService();
            mService.setActivityHandler(mHandler);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        if (!mBluetoothAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        updateUI();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        if (ANSChangeReceiver != null)
            unregisterReceiver(ANSChangeReceiver);
        if (mServiceConnection != null)
            unbindService(mServiceConnection);
        mService = null;
        stopService(new Intent(this, ANPServerService.class));
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    private void updateUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setUiState();
            }
        });
    }
    private void setUiState() {
        Log.d(TAG, "... setUiState.mState" + mState);
                if (mState == STATE_CONNECTED) {
                    mBtnSelectDevice.setEnabled(false);
                    mBtndisconnect.setEnabled(true);
                    mSendEvent.setEnabled(true);
                } else if (mState == STATE_DISCONNECTED) {
                    mBtnSelectDevice.setEnabled(true);
                    mBtndisconnect.setEnabled(false);
                    mSendEvent.setEnabled(false);
                }
                findViewById(R.id.btn_remove_bond).setEnabled(
                        mDevice != null && mDevice.getBondState() == BluetoothDevice.BOND_BONDED);
                if (mDevice != null)
                    mTVDeviceName.setText(mDevice.getName());
                else
                    mTVDeviceName.setText(R.string.no_device);
    }

    private void setUiStateForBTOff() {
        mBtnSelectDevice.setEnabled(true);
        mBtndisconnect.setEnabled(false);
        mSendEvent.setEnabled(false);
        findViewById(R.id.btn_remove_bond).setEnabled(false);

        if (mDevice != null
                && mDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
            mTVDeviceName.setText(R.string.no_device);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_select:
            if (!mBluetoothAdapter.isEnabled()) {
                Log.i(TAG, "onClick - BT not enabled yet");
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            }
            else {
                Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
            }
            break;

        case R.id.btn_disconnect:
            if (mService != null)
                mService.disconnect(mDevice);
            break;

        case R.id.button_send_alert:
            Log.e("MainActivity", "Clicked");
            if (mService != null)
                mService.sendAlert(mDevice);
            break;
        default:
            Log.e(TAG,"wrong Click event");
            break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);

        switch (requestCode) {
        case REQUEST_ENABLE_BT:
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        case REQUEST_SELECT_DEVICE:
            if (resultCode == Activity.RESULT_OK && data != null) {
                showMessage("selectDevice");
                String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
                setUiState();
                mService.connect(mDevice, false);
            }
            break;
        default:
            Log.e(TAG,"Wrong request Code");
            break;
        }
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (mState == STATE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }
        else {
            new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.popup_title)
            .setMessage(R.string.popup_message)
            .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            })
            .setNegativeButton(R.string.popup_no, null)
            .show();
        }
    }
}
