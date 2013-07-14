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

package com.samsung.ble.pxpmonitor;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

public class ProximityMonitorActivity extends Activity implements RadioGroup.OnCheckedChangeListener {
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int STATE_READY = 10;
    public static final String TAG = "pxpmonitor";
    private static final int PXP_PROFILE_CONNECTED = 20;
    private static final int PXP_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;

    TextView TxPowerValue, mRemoteRssiVal;
    private int mState = PXP_PROFILE_DISCONNECTED;
    RadioGroup mRg;

    private ProximityService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private static int mAlertLevel = ProximityService.HIGH_ALERT;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        TxPowerValue = ((TextView) findViewById(R.id.statusValue1));
        init();

        ((Button) findViewById(R.id.btn_select)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else {
                    Intent newIntent = new Intent(ProximityMonitorActivity.this, DeviceListActivity.class);
                    startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                }
            }
        });

        ((Button) findViewById(R.id.btn_remove_bond)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.removeBond(mDevice);
            }
        });

        ((Button) findViewById(R.id.btn_connect)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.connect(mDevice, false);
            }
        });

        ((Button) findViewById(R.id.btn_disconnect)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.disconnect(mDevice);
            }
        });

        ((Button) findViewById(R.id.btn_ias_alert)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.writeIasAlertLevel(mDevice, mAlertLevel);
            }
        });

        ((Button) findViewById(R.id.btn_lls_alert)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.writeLlsAlertLevel(mDevice, mAlertLevel);
            }
        });

        ((Button) findViewById(R.id.btn_write_Tx_Noty)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.enableTxPowerNoti(mDevice);
            }
        });

        ((Button) findViewById(R.id.btn_readrssi)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.readRssi(mDevice);
            }
        });

        mRemoteRssiVal = (TextView) findViewById(R.id.rssival);

        ((Button) findViewById(R.id.btn_Txpower)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.ReadTxPower(mDevice);
            }
        });

        mRg = (RadioGroup) findViewById(R.id.radioGroup1);
        mRg.setOnCheckedChangeListener(this);

        // Set initial UI state
        setUiState();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((ProximityService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            mService.setActivityHandler(mHandler);
        }

        public void onServiceDisconnected(ComponentName classname) {
            mService.disconnect(mDevice);
            mService = null;
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            final Bundle data = msg.getData();
            switch (msg.what) {
            case ProximityService.PXP_CONNECT_MSG:
                runOnUiThread(new Runnable() {
                    public void run() {
                        if(mDevice!=null && mDevice.getAddress().equals(data.getString(BluetoothDevice.EXTRA_DEVICE))){
                            Log.d(TAG, "PXP_CONNECT_MSG");
                            mState = PXP_PROFILE_CONNECTED;
                            setUiState();
                        }
                    }
                });
                break;
            case ProximityService.PXP_DISCONNECT_MSG:
                runOnUiThread(new Runnable() {
                    public void run() {
                        if(mDevice!=null && mDevice.getAddress().equals(data.getString(BluetoothDevice.EXTRA_DEVICE))){
                            Log.d(TAG, "PXP_DISCONNECT_MSG");
                            mState = PXP_PROFILE_DISCONNECTED;
                            setUiState();
                        }
                    }
                });
                break;

            case ProximityService.PXP_READY_MSG:
                Log.d(TAG, "PXP_READY_MSG");
                runOnUiThread(new Runnable() {
                    public void run() {
                        mState = STATE_READY;
                        setUiState();
                    }
                });
                break;

            case ProximityService.PXP_VALUE_MSG:
                Log.d(TAG, "PXP_VALUE_MSG");
                final byte[] value = data.getByteArray(ProximityService.EXTRA_VALUE);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            TxPowerValue.setText("" + value[0]);
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
                break;
            case ProximityService.GATT_CHARACTERISTIC_RSSI_MSG:
                Log.d(TAG, "GATT_CHARACTERISTIC_RSSI_MSG");
                final BluetoothDevice RemoteRssidevice = data.getParcelable(ProximityService.EXTRA_DEVICE);
                final int rssi = data.getInt(ProximityService.EXTRA_RSSI);
                Log.d(TAG, "rssi value is" + rssi + " and remote device is" + RemoteRssidevice);
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (mDevice != null && RemoteRssidevice != null && mDevice.equals(RemoteRssidevice)) {
                            if (rssi != 0) {
                                mRemoteRssiVal.setText("Rssi Value =" + String.valueOf(rssi));
                            }
                        }
                    }
                });
                break;
            case ProximityService.PROXIMITY_ALERT_LEVEL_CHANGED_MSG:
                final byte[] value1 = data.getByteArray(ProximityService.EXTRA_VALUE);
                final byte alertLevel = value1[0];
                Log.d(TAG, "PROXIMITY_ALERT_LEVEL_CHANGED_MSG value of alertlevel is " + value1[0]);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (alertLevel != ProximityService.NO_ALERT) {
                            try {
                                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                                r.play();
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            }

                            switch (alertLevel) {
                            case ProximityService.LOW_ALERT:
                                showMessage(getString(R.string.low_alert));
                                break;
                            case ProximityService.HIGH_ALERT:
                                showMessage(getString(R.string.high_alert));
                                break;
                            default:
                                Log.e(TAG,"wrong alert level");
                                break;
                            }
                        }
                    }
                });
                break;
            default:
                super.handleMessage(msg);
            }
        }
    };

    private final BroadcastReceiver proximityStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = mIntent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "BluetoothDevice.ACTION_BOND_STATE_CHANGED");
                if (device.equals(mDevice)) {
                    runOnUiThread(new Runnable() {
                        public void run() {
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
                                mState = PXP_PROFILE_DISCONNECTED;
                                setUiStateForBTOff();
                            }
                        }
                    });
            }
        }
    };

    private void init() {
        Intent bindIntent = new Intent(this, ProximityService.class);
        startService(bindIntent);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(proximityStatusChangeReceiver, filter);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() mService= " + mService);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        try {
            unregisterReceiver(proximityStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        stopService(new Intent(this, ProximityService.class));
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        updateUi();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

        case REQUEST_SELECT_DEVICE:
            if (resultCode == Activity.RESULT_OK && data != null) {
                String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
                Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                setUiState();
                mService.connect(mDevice, false);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        default:
            Log.e(TAG, "wrong request code");
            break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.radio0)
            mAlertLevel = ProximityService.HIGH_ALERT;
        else if (checkedId == R.id.radio1)
            mAlertLevel = ProximityService.LOW_ALERT;
        else if (checkedId == R.id.radio2)
            mAlertLevel = ProximityService.NO_ALERT;
    }

    private void updateUi() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setUiState();
            }
        });
    }

    private void setUiStateForBTOff() {
        Log.d(TAG, "... setUiStateForBTOff.mState" + mState);
        findViewById(R.id.btn_select).setEnabled(true);
        findViewById(R.id.btn_ias_alert).setEnabled(false);
        findViewById(R.id.btn_lls_alert).setEnabled(false);
        findViewById(R.id.btn_disconnect).setEnabled(false);
        findViewById(R.id.btn_connect).setEnabled(false);
        findViewById(R.id.btn_Txpower).setEnabled(false);
        findViewById(R.id.btn_write_Tx_Noty).setEnabled(false);
        findViewById(R.id.btn_readrssi).setEnabled(false);
        findViewById(R.id.btn_remove_bond).setEnabled(false);

        if (mDevice != null
                && mDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
            Log.i(TAG, "in no device zone");
            ((TextView) findViewById(R.id.deviceName))
                    .setText(R.string.no_device);
        }
        mRemoteRssiVal.setVisibility(View.VISIBLE);
    }

    private void setUiState() {
        findViewById(R.id.btn_select).setEnabled(mState == PXP_PROFILE_DISCONNECTED);
        Log.d(TAG, "... setUiState.mState" + mState);
        findViewById(R.id.btn_ias_alert).setEnabled(mState == STATE_READY);
        findViewById(R.id.btn_lls_alert).setEnabled(mState == STATE_READY);
        findViewById(R.id.btn_disconnect).setEnabled(mState == STATE_READY || mState == PXP_PROFILE_CONNECTED);
        if (mDevice != null) {
            findViewById(R.id.btn_connect).setEnabled(mState == PXP_PROFILE_DISCONNECTED && mDevice.getBondState() == BluetoothDevice.BOND_BONDED);
        }
        else findViewById(R.id.btn_connect).setEnabled(false);
        findViewById(R.id.btn_Txpower).setEnabled(mState == STATE_READY);
        findViewById(R.id.btn_write_Tx_Noty).setEnabled(mState == STATE_READY);
        findViewById(R.id.btn_readrssi).setEnabled(mState == STATE_READY);
        findViewById(R.id.btn_remove_bond).setEnabled(
                mDevice != null && mDevice.getBondState() == BluetoothDevice.BOND_BONDED);

        if (mDevice != null && mDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
            Log.i(TAG, "in no device zone");
            ((TextView) findViewById(R.id.deviceName)).setText(R.string.no_device);
        }
        if (mDevice != null && mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName());
        }
        mRemoteRssiVal.setVisibility(View.VISIBLE);
        TextView status = ((TextView) findViewById(R.id.statusValue));

        switch (mState) {
        case PXP_PROFILE_CONNECTED:
            Log.i(TAG, "STATE_CONNECTED::device name"+mDevice.getName());
            status.setText(R.string.connected);
            ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName());
            break;
        case PXP_PROFILE_DISCONNECTED:
            Log.i(TAG, "disconnected");
            status.setText(R.string.disconnected);
            mRemoteRssiVal.setText(R.string.Noupdate);
            TxPowerValue.setText(R.string.Noupdate);
            break;
        case STATE_READY:
            status.setText(R.string.ready);
            ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName());
            break;
        default:
            Log.e(TAG, "wrong mState");
            break;
        }
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (mState == STATE_READY) {
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
