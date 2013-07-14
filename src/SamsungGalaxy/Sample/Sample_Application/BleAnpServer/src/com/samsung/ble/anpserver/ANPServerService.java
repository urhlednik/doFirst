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

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.samsung.android.sdk.bt.gatt.BluetoothGattAdapter;
import com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic;
import com.samsung.android.sdk.bt.gatt.BluetoothGattDescriptor;
import com.samsung.android.sdk.bt.gatt.BluetoothGattServer;
import com.samsung.android.sdk.bt.gatt.BluetoothGattServerCallback;
import com.samsung.android.sdk.bt.gatt.BluetoothGattService;
import com.samsung.android.sdk.bt.gatt.MutableBluetoothGattCharacteristic;
import com.samsung.android.sdk.bt.gatt.MutableBluetoothGattDescriptor;
import com.samsung.android.sdk.bt.gatt.MutableBluetoothGattService;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.IBinder;
import android.provider.CallLog;
import android.util.Log;
import android.widget.Toast;

public class ANPServerService extends Service {

    public static final String ACTION_CONNECT = "com.siso.ble.ble.anpserver.connect";
    public static final String ACTION_DISCONNECT = "com.siso.ble.anpserver.disconnect";
    private static final boolean D = true;

    private static final UUID IMMEDIATE_ALERT_UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
    private static final UUID ALERT_LEVEL_UUID = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");

    private static final UUID ANS_UUID = UUID.fromString("00001811-0000-1000-8000-00805f9b34fb");
    private static final UUID AlertNotiControlPoint_UUID = UUID.fromString("00002a44-0000-1000-8000-00805f9b34fb");
    private static final UUID UnreadAlertStatus_UUID = UUID.fromString("00002a45-0000-1000-8000-00805f9b34fb");
    private static final UUID newAlert_UUID = UUID.fromString("00002a46-0000-1000-8000-00805f9b34fb");
    private static final UUID newAlertCat_UUID = UUID.fromString("00002a47-0000-1000-8000-00805f9b34fb");
    private static final UUID unreadAlertCat_UUID = UUID.fromString("00002a48-0000-1000-8000-00805f9b34fb");
    public static final UUID CCC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public int mState1 = 1;
    private static final String TAG = "ANPServerService";
    private Handler mDeviceListHanlder = null;
    private Handler mActivityHanlder = null;

    public static final int ANS_CONNECT_MSG = 21;
    public static final int ANS_DISCONNECT_MSG = 22;
    public static final int GATT_DEVICE_FOUND_MSG = 25;

    private int mState = BluetoothProfile.STATE_DISCONNECTED;

    private BluetoothAdapter mBtAdapter = null;
    public BluetoothGattServer mBluetoothGattServer = null;
    private BluetoothDevice mDevice = null;
    private MutableBluetoothGattCharacteristic mNewAlert = null;
    private MutableBluetoothGattCharacteristic mUnreadAlert = null;
    private MutableBluetoothGattCharacteristic mAlertNotiControlPoint = null;
    private static byte[] newAlertcccDescVal = { 0, 0 };
    private static byte[] unreadAlertcccDescVal = { 0, 0 };

    public static final String GATT_DEVICE_FOUND = "com.samsung.gatt.device_found";
    public static final int DEVICE_SOURCE_SCAN = 0;
    public static final int DEVICE_SOURCE_BONDED = 1;
    public static final int DEVICE_SOURCE_CONNECTED = 2;
    public static final String EXTRA_DEVICE = "DEVICE";
    public static final String EXTRA_RSSI = "RSSI";
    public static final String EXTRA_SOURCE = "SOURCE";
    public static final String EXTRA_ADDR = "ADDRESS";
    public static final String EXTRA_CONNECTED = "CONNECTED";
    public static final String EXTRA_STATUS = "STATUS";
    public static final String EXTRA_UUID = "UUID";
    public static final String EXTRA_VALUE = "VALUE";

    public static int incomingCallunreadCount = 0;
    public static int newAlert = 0;
    public static int missedCallunreadCount = 0;
    public static int newAlertMissedCall = 0;
    private static int missCallCount = 0;
    public static int newUnreadSmsMmsCount = 0;
    private static int unreadSmsMmsCount = 0;
    public static int newAlertSmsMms = 0;
    private static int incomingCallCount = 0;

    public static String targetString_incomingCall = null;
    public static String targetString_missedCall = null;
    public static String targetString_mmsSms = null;
    private boolean IsEmailAlertIncomingAlertEnabled = false;
    private boolean IsEmailAlertUnreadAlertEnabled = false;
    private boolean IsEmailAlertIncomingNotifyImmAlertEnabled = false;
    private boolean IsEmailAlertUnreadNotifyImmAlertEnabled = false;

    private boolean IsSmsMmsIncomingAlertEnabled = false;
    private boolean IsSmsMmsUnreadAlertEnabled = false;
    private boolean IsSmsMmsAlertIncomingNotifyImmAlertEnabled = false;
    private boolean IsSmsMmsAlertUnreadNotifyImmAlertEnabled = false;
    private boolean IsIncomingCallIncomingAlertEnabled = false;
    private boolean IsIncomingCallunreadAlertEnabled = false;
    private boolean IsIncomingCallIncomingNotifyImmAlertEnabled = false;
    private boolean IsIncomingCallUnreadNotifyImmAlertEnabled = false;
    private boolean IsMissedCallIncomingAlertEnabled = false;
    private boolean IsMissedCallUnreadAlertEnabled = false;
    private boolean IsMissedCallIncomingNotifyImmAlertEnabled = false;
    private boolean IsMissedCallUnreadNotifyImmAlertEnabled = false;
    public static int statusOk = 0x0000;
    public static int statusNotOk = 0x0006;
    public static int dummyCount = 0x01;
    public static int callCategory = 0x03;
    public static String dummyName = "Praveen";
    public static int newAlertCatValue = 0x3a;
    public static int EnableNotification = 0x01;
    public static byte[] Value = { 0x3a,00 };

    class AnsCategoryId {
        public static final int SimpleAlert = 0;
        public static final int Email = 1;
        public static final int News = 2;
        public static final int Call = 3;
        public static final int MissedCall = 4;
        public static final int SMS_MMS = 5;
        public static final int VoiceMail = 6;
        public static final int Schedule = 7;
    }

    private static final Uri CONTENT_URI_SMS_INBOX = Uri.parse("content://sms/inbox");
    private final Uri CONTENT_URI_MMS_INBOX = Uri.parse("content://mms/inbox");

    private boolean mPendingSMSMMSUpdate = false;
    private boolean mPendingMissedCallUpdate = false;
    private boolean mPendingIncomingCallUpdate = false;

    private SMSMMSUpdateThread mSMSMMSUpdateThread;
    private MissedCallUpdateThread mMissedCallUpdateThread;
    private IncomingCallUpdateThread mIncomingCallUpdateThread;

    private BluetoothSMSMMSContentObserver mSMSMMSObserver;
    private CallContentObserver mCallObserver;

    private class BluetoothSMSMMSContentObserver extends ContentObserver {
        private BluetoothSMSMMSContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            updateFromSMSMMSProvider();
        }
    }

    private class CallContentObserver extends ContentObserver {
        private CallContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            updateFromMissedCallProvider();
            updateFromIncomingCallProvider();
        }
    }

    public class LocalBinder extends Binder {
        ANPServerService getService() {
            Log.i(TAG, "getService");
            return ANPServerService.this;
        }
    }

    private final IBinder binder = new LocalBinder();

    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate() called");
        if (mBtAdapter == null) {
            mBtAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBtAdapter == null) {
                Log.i(TAG, "adapter is null");
                return;
            }
        }

        if (mBluetoothGattServer == null) {
            Log.i(TAG, "mBluetoothGattServer::null");
            Log.i(TAG, "getting proxy::");
            BluetoothGattAdapter.getProfileProxy(this, mProfileServiceListener, BluetoothGattAdapter.GATT_SERVER);
        }

        mSMSMMSObserver = new BluetoothSMSMMSContentObserver();
        mCallObserver = new CallContentObserver();
        getContentResolver().registerContentObserver(Uri.parse("content://mms-sms"), true, mSMSMMSObserver);
        getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, mCallObserver);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy() called");
        if (mBtAdapter != null && mBluetoothGattServer != null) {
            BluetoothGattAdapter.closeProfileProxy(BluetoothGattAdapter.GATT_SERVER, mBluetoothGattServer);
        }
        getContentResolver().unregisterContentObserver(mSMSMMSObserver);
        getContentResolver().unregisterContentObserver(mCallObserver);
        mSMSMMSObserver = null;
        mCallObserver = null;
        super.onDestroy();
    }

    /**
     * Profile service connection listener
     */
    private BluetoothProfile.ServiceListener mProfileServiceListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Log.i(TAG, "onServiceConnected::register App");
            if (proxy == null) {
                Log.i(TAG, "proxy::null");
            }
            mBluetoothGattServer = (BluetoothGattServer) proxy;
            mBluetoothGattServer.registerApp(mGattCallbacks);
            Log.i(TAG, "onServiceConnected::register App complted");
        }

        @Override
        public void onServiceDisconnected(int profile) {
            Log.i(TAG, "onServiceDisconnected ");
            if (mBluetoothGattServer != null) {
                mBluetoothGattServer.unregisterApp();
                mBluetoothGattServer = null;
            }
        }
    };

    /**
     * GATT callbacks
     */
    private BluetoothGattServerCallback mGattCallbacks = new BluetoothGattServerCallback() {
        @Override
        public void onScanResult(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d(TAG, "onScanResult() - device=" + device + ", rssi=" + rssi);
            if (!checkIfBroadcastMode(scanRecord)) {
                Bundle mBundle = new Bundle();
                Message msg = Message.obtain(mDeviceListHanlder, GATT_DEVICE_FOUND_MSG);
                mBundle.putParcelable(BluetoothDevice.EXTRA_DEVICE, device);
                mBundle.putInt(EXTRA_RSSI, rssi);
                mBundle.putInt(EXTRA_SOURCE, DEVICE_SOURCE_SCAN);
                msg.setData(mBundle);
                msg.sendToTarget();
            } else
                Log.i(TAG, "device =" + device + " is in Brodacast mode, hence not displaying");
        }

        @Override
        public void onAppRegistered(int status) {
            Log.d(TAG, "onAppRegistered");
            if (status == 0) {
                registerAttributes();
                updateFromSMSMMSProvider();
                updateFromMissedCallProvider();
                updateFromIncomingCallProvider();
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            Log.d(TAG, "onConnectionStateChange (" + device.getAddress() + ")");
            if (newState == BluetoothProfile.STATE_CONNECTED && mBluetoothGattServer != null && mDevice!=null && mDevice.equals(device)) {
                Bundle mBundle = new Bundle();
                Message msg = Message.obtain(mActivityHanlder, ANS_CONNECT_MSG);
                mBundle.putParcelable(BluetoothDevice.EXTRA_DEVICE, device);
                mDevice = device;
                msg.setData(mBundle);
                msg.sendToTarget();
            }

            if (newState == BluetoothProfile.STATE_DISCONNECTED && mBluetoothGattServer != null && mDevice!=null && mDevice.equals(device)) {
                IsSmsMmsIncomingAlertEnabled = false;
                IsSmsMmsUnreadAlertEnabled = false;
                IsSmsMmsAlertIncomingNotifyImmAlertEnabled = false;
                IsSmsMmsAlertUnreadNotifyImmAlertEnabled = false;
                IsIncomingCallIncomingAlertEnabled = false;
                IsIncomingCallunreadAlertEnabled = false;
                IsIncomingCallIncomingNotifyImmAlertEnabled = false;
                IsIncomingCallUnreadNotifyImmAlertEnabled = false;
                IsMissedCallIncomingAlertEnabled = false;
                IsMissedCallUnreadAlertEnabled = false;
                IsMissedCallIncomingNotifyImmAlertEnabled = false;
                IsMissedCallUnreadNotifyImmAlertEnabled = false;
                Message msg = Message.obtain(mActivityHanlder, ANS_DISCONNECT_MSG);
                msg.sendToTarget();
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicReadRequest");
            if (mBluetoothGattServer != null) {
                byte[] value = Value;
                mBluetoothGattServer.sendResponse(device, requestId, statusOk, offset, value);
            }
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset,
                byte[] value) {
            Log.d(TAG, "onCharacteristicWriteRequest" + "preparedWrite" + preparedWrite + "responseNeeded"
                    + responseNeeded + "...category Id is " + value[1] + "and requested feature is " + value[0]);
            Log.d(TAG, "value[0]" + value[0] + "value[1]" + value[1]);
            boolean isOk = false;
            if (mBluetoothGattServer != null) {
             isOk =true;
                switch (value[1]) {
                case 1: {
                    switch (value[0]) {
                    case 0:
                        IsEmailAlertIncomingAlertEnabled = true;
                        break;
                    case 1:
                        IsEmailAlertUnreadAlertEnabled = true;
                        break;
                    case 2:
                        IsEmailAlertIncomingAlertEnabled = false;
                        break;
                    case 3:
                        IsEmailAlertUnreadAlertEnabled = false;
                        break;
                    case 4:
                        IsEmailAlertIncomingNotifyImmAlertEnabled = true;
                        break;
                    case 5:
                        IsEmailAlertUnreadNotifyImmAlertEnabled = true;
                        break;

                    default:
                        isOk = false;
                        break;
                    }
                }
                    break;
                case 3: {
                    switch (value[0]) {
                    case 0:
                        IsIncomingCallIncomingAlertEnabled = true;
                        break;
                    case 1:
                        IsIncomingCallunreadAlertEnabled = true;
                        break;
                    case 2:
                        IsIncomingCallIncomingAlertEnabled = false;
                        break;
                    case 3:
                        IsIncomingCallunreadAlertEnabled = false;
                        break;
                    case 4:
                        IsIncomingCallIncomingNotifyImmAlertEnabled = true;
                        break;
                    case 5:
                        IsIncomingCallUnreadNotifyImmAlertEnabled = true;
                        break;

                    default:
                        isOk = false;
                        break;
                    }
                }
                    break;
                case 4: {
                    switch (value[0]) {
                    case 0:
                        IsMissedCallIncomingAlertEnabled = true;
                        break;
                    case 1:
                        IsMissedCallUnreadAlertEnabled = true;
                        break;
                    case 2:
                        IsMissedCallIncomingAlertEnabled = false;
                        break;
                    case 3:
                        IsMissedCallUnreadAlertEnabled = false;
                        break;
                    case 4:
                        IsMissedCallIncomingNotifyImmAlertEnabled = true;
                        break;
                    case 5:
                        IsMissedCallUnreadNotifyImmAlertEnabled = true;
                        break;

                    default:
                        isOk = false;
                        break;
                    }
                }
                    break;
                case 5: {
                    byte CharVal = value[0];
                    switch (CharVal) {
                    case 0x0:
                        IsSmsMmsIncomingAlertEnabled = true;
                        break;
                    case 0x1:
                        IsSmsMmsUnreadAlertEnabled = true;
                        break;
                    case 0x2:
                        IsSmsMmsIncomingAlertEnabled = false;
                        break;
                    case 0x3:
                        IsSmsMmsUnreadAlertEnabled = false;
                        break;
                    case 0x4:
                        IsSmsMmsAlertIncomingNotifyImmAlertEnabled = true;
                        break;
                    case 0x5:
                        IsSmsMmsAlertUnreadNotifyImmAlertEnabled = true;
                        break;
                    default:
                        isOk = false;
                        break;
                    }
                }
                    break;
                case (byte) 0xff:
                    Log.d(TAG, "Category ID is value[0] all values set true" + value[0]);
                    IsSmsMmsIncomingAlertEnabled = true;
                    IsSmsMmsUnreadAlertEnabled = true;
                    IsSmsMmsAlertIncomingNotifyImmAlertEnabled = true;
                    IsSmsMmsAlertUnreadNotifyImmAlertEnabled = true;
                    IsIncomingCallIncomingAlertEnabled = true;
                    IsIncomingCallunreadAlertEnabled = true;
                    IsIncomingCallIncomingNotifyImmAlertEnabled = true;
                    IsIncomingCallUnreadNotifyImmAlertEnabled = true;
                    IsMissedCallIncomingAlertEnabled = true;
                    IsMissedCallUnreadAlertEnabled = true;
                    IsMissedCallIncomingNotifyImmAlertEnabled = true;
                    IsMissedCallUnreadNotifyImmAlertEnabled = true;
                    break;

                default:
                    isOk = false;
                    break;

                }
                if (responseNeeded) {
                    if (isOk == true) {
                        mBluetoothGattServer.sendResponse(device, requestId, statusOk, offset, value);
                    } else {
                        mBluetoothGattServer.sendResponse(device, requestId, statusNotOk, offset, value);
                    }
                }

            }
            Log.d(TAG, "...IsSmsMmsIncomingAlertEnabled" + IsSmsMmsIncomingAlertEnabled
                    + ".........IsMissedCallUnreadNotifyImmAlertEnabled" + IsMissedCallUnreadNotifyImmAlertEnabled
                    + "IsSmsMmsUnreadAlertEnabled" + IsSmsMmsUnreadAlertEnabled
                    + ".........IsSmsMmsAlertIncomingNotifyImmAlertEnabled"
                    + IsSmsMmsAlertIncomingNotifyImmAlertEnabled + ".......IsSmsMmsAlertUnreadNotifyImmAlertEnabled"
                    + IsSmsMmsAlertUnreadNotifyImmAlertEnabled + "........IsIncomingCallIncomingAlertEnabled"
                    + IsIncomingCallIncomingAlertEnabled + ".......IsIncomingCallunreadAlertEnabled"
                    + IsIncomingCallunreadAlertEnabled + "........IsMissedCallIncomingAlertEnabled"
                    + IsMissedCallIncomingAlertEnabled + "..............IsMissedCallIncomingNotifyImmAlertEnabled"
                    + IsMissedCallIncomingNotifyImmAlertEnabled);
            updateImmediateAlerts();
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor,
                boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            if (descriptor.getCharacteristic().equals(mNewAlert))
                newAlertcccDescVal = value;
            if (descriptor.getCharacteristic().equals(mUnreadAlert))
                unreadAlertcccDescVal = value;
            if (responseNeeded)
                mBluetoothGattServer.sendResponse(device, requestId, statusOk, offset, value);
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset,
                BluetoothGattDescriptor descriptor) {
            Log.e(TAG, "onDescriptorReadRequest");
            if (descriptor.getCharacteristic().equals(mNewAlert))
                mBluetoothGattServer.sendResponse(device, requestId, statusOk, 0, newAlertcccDescVal);
            if (descriptor.getCharacteristic().equals(mUnreadAlert))
                mBluetoothGattServer.sendResponse(device, requestId, statusOk, 0, unreadAlertcccDescVal);
        }
    };

    public void scan(boolean start) {
        if (mBluetoothGattServer == null)
            return;

        if (start) {
            mBluetoothGattServer.startScan();
        } else {
            mBluetoothGattServer.stopScan();
        }
    }

    public void connect(BluetoothDevice device, boolean autoconnect) {
        Log.d(TAG, "connect");
        if (mBluetoothGattServer != null) {
            showMessage("connecttoGattServer");
            mDevice = device;//set remote device to map corresponding connection callback, added for multiconnection issue.
            mBluetoothGattServer.connect(device, autoconnect);
        }
    }

    public void disconnect(BluetoothDevice device) {
        if (mBluetoothGattServer != null) {
            mBluetoothGattServer.cancelConnection(device);
        }
    }

    public void sendAlert(BluetoothDevice device) {
        Log.d(TAG, "sendAlert");
        byte[] value = null;
        byte cat = (byte) callCategory;
        byte cnt = (byte) dummyCount;

        try {
            String s = dummyName;
            value = s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] attVal = new byte[value.length + 2];

        attVal[0] = cat;
        attVal[1] = cnt;
        for (int i = 0; i < value.length; i++)
            attVal[i + 2] = value[i];

        mNewAlert.setValue(attVal);
        mBluetoothGattServer.notifyCharacteristicChanged(device, mNewAlert, false);

    }

    public int getState(BluetoothDevice device) {
        int state = BluetoothProfile.STATE_DISCONNECTED;

        if (mBluetoothGattServer != null)
            state = mBluetoothGattServer.getConnectionState(device);

        return state;
    }

    private void registerAttributes() {
        Log.d(TAG, "registerAttributes");
        // Alert Notification Control Point Characteristic
        mAlertNotiControlPoint = new MutableBluetoothGattCharacteristic(AlertNotiControlPoint_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_READ |BluetoothGattCharacteristic.PERMISSION_WRITE);

        MutableBluetoothGattDescriptor newAlertCCC = new MutableBluetoothGattDescriptor(CCC, BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE);
        MutableBluetoothGattDescriptor unreadAlertCCC = new MutableBluetoothGattDescriptor(CCC, BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE);

        mUnreadAlert = new MutableBluetoothGattCharacteristic(UnreadAlertStatus_UUID,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_READ);
        unreadAlertcccDescVal = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
        unreadAlertCCC.setValue(unreadAlertcccDescVal);
        mUnreadAlert.addDescriptor(unreadAlertCCC);

        mNewAlert = new MutableBluetoothGattCharacteristic(newAlert_UUID, BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        newAlertcccDescVal = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
        newAlertCCC.setValue(newAlertcccDescVal);
        mNewAlert.addDescriptor(newAlertCCC);

        byte[] value = { (byte)newAlertCatValue };
        MutableBluetoothGattCharacteristic newAlertCat = new MutableBluetoothGattCharacteristic(newAlertCat_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ);
        newAlertCat.setValue(value);
        MutableBluetoothGattCharacteristic unreadAlertCat = new MutableBluetoothGattCharacteristic(unreadAlertCat_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ);

        unreadAlertCat.setValue(value);
        MutableBluetoothGattService alertNotificationServie = new MutableBluetoothGattService(ANS_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        alertNotificationServie.addCharacteristic(mAlertNotiControlPoint);
        alertNotificationServie.addCharacteristic(mUnreadAlert);
        alertNotificationServie.addCharacteristic(mNewAlert);
        alertNotificationServie.addCharacteristic(newAlertCat);
        alertNotificationServie.addCharacteristic(unreadAlertCat);

        mBluetoothGattServer.addService(alertNotificationServie);
        Log.i(TAG, "register attributes:: successfull");

    }

    private void updateFromSMSMMSProvider() {
        synchronized (ANPServerService.this) {
            mPendingSMSMMSUpdate = true;
            if (mSMSMMSUpdateThread == null) {
                mSMSMMSUpdateThread = new SMSMMSUpdateThread();
                mSMSMMSUpdateThread.start();
            }
        }
    }

    private void updateFromMissedCallProvider() {
        synchronized (ANPServerService.this) {
            mPendingMissedCallUpdate = true;
            if (mMissedCallUpdateThread == null) {
                mMissedCallUpdateThread = new MissedCallUpdateThread();
                mMissedCallUpdateThread.start();
            }
        }
    }

    private void updateFromIncomingCallProvider() {
        synchronized (ANPServerService.this) {
            mPendingIncomingCallUpdate = true;
            if (mIncomingCallUpdateThread == null) {
                mIncomingCallUpdateThread = new IncomingCallUpdateThread();
                mIncomingCallUpdateThread.start();
            }
        }
    }

    private class IncomingCallUpdateThread extends Thread {
        public IncomingCallUpdateThread() {
            super("Incoming Call Update Thread");
        }

        @Override
        public void run() {
            Log.i(TAG, "IncomingCallUpdateThread");
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            for (;;) {
                synchronized (ANPServerService.this) {
                    if (mIncomingCallUpdateThread != this)
                        throw new IllegalStateException("multiple Incoming Call UpdateThreads running");
                    if (!mPendingIncomingCallUpdate) {
                        mIncomingCallUpdateThread = null;
                        break;
                    }
                    mPendingIncomingCallUpdate = false;
                    String[] projection = new String[] { "name", "number", "date" };
                    String selection = "type = 1 AND is_read = 0";
                    Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, projection, selection, null,
                            null);
                    if (cursor == null) {
                        mIncomingCallUpdateThread = null;
                        return;
                    }
                    cursor.moveToFirst();
                    incomingCallunreadCount = (byte) cursor.getCount();
                    cursor.close();
                    try {
                        if (incomingCallunreadCount > 0) {
                            if (D)
                                Log.i(TAG, "incomingCallunreadCount  =" + incomingCallunreadCount);
                            Log.i(TAG, "unreadAlertcccDescVal::->" + unreadAlertcccDescVal[0]
                                    + "IsIncomingCallunreadAlertEnabled :->" + IsIncomingCallunreadAlertEnabled);
                            if (unreadAlertcccDescVal[0] == EnableNotification && IsIncomingCallunreadAlertEnabled == true)
                                updateUnreadAlert(AnsCategoryId.Call, incomingCallunreadCount);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }

                    String selection_newAlert = "type =1";
                    Cursor cursor_newAlert = getContentResolver().query(CallLog.Calls.CONTENT_URI, projection,
                            selection_newAlert, null, null);
                    if (cursor_newAlert == null) {
                        if (D)
                            Log.e(TAG, "Incoming -cursor_newAlert = null");
                        mIncomingCallUpdateThread = null;
                        return;
                    }
                    cursor_newAlert.moveToFirst();
                    int newIncomingCallCount = (byte) cursor_newAlert.getCount();
                    if (newIncomingCallCount > 0) {
                        String person_name = cursor_newAlert.getString(cursor_newAlert.getColumnIndex("name"));
                        String number = null;
                        if (person_name == null) {
                            number = cursor_newAlert.getString(cursor_newAlert.getColumnIndex("number"));
                        }
                        if (incomingCallCount == 0) {
                            incomingCallCount = newIncomingCallCount;
                            try {
                                targetString_incomingCall = ((person_name == null) ? number : person_name);
                                if (targetString_incomingCall == null) {
                                    targetString_incomingCall = "You have Incoming call";
                                }
                                if (D)
                                    Log.i(TAG, "sending new alert with new alert count = " + newAlert + "And string ="
                                            + targetString_incomingCall);
                                Log.i(TAG, "newAlertcccDescVal::->" + newAlertcccDescVal[0]
                                        + "IsIncomingCallIncomingAlertEnabled :->" + IsIncomingCallIncomingAlertEnabled);
                                if (newAlertcccDescVal[0] == EnableNotification && IsIncomingCallIncomingAlertEnabled == true)
                                    updateNewAlert(AnsCategoryId.Call, incomingCallCount, targetString_incomingCall);

                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            }
                        } else {
                            if (newIncomingCallCount > incomingCallCount) {
                                try {
                                    targetString_incomingCall = ((person_name == null) ? number : person_name);
                                    if (targetString_incomingCall == null) {
                                        targetString_incomingCall = "You have Incoming call";
                                    }
                                    newAlert = newIncomingCallCount - incomingCallCount;
                                    if (D)
                                        Log.i(TAG, "sending new alert with new alert count = " + newAlert
                                                + "And string =" + targetString_incomingCall);
                                    if (newAlertcccDescVal[0] == EnableNotification && IsIncomingCallIncomingAlertEnabled == true)
                                        updateNewAlert(AnsCategoryId.Call, newAlert, targetString_incomingCall);
                                    incomingCallCount = newIncomingCallCount;
                                } catch (Exception e) {
                                    Log.e(TAG, e.toString());
                                }
                            }
                        }
                    }
                    cursor_newAlert.close();

                }
            }
        }

    }

    private class MissedCallUpdateThread extends Thread {
        public MissedCallUpdateThread() {
            super("Missed Call Update Thread");
        }

        @Override
        public void run() {
            Log.i(TAG, "MissedCallUpdateThread started");
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            for (;;) {
                synchronized (ANPServerService.this) {
                    if (mMissedCallUpdateThread != this)
                        throw new IllegalStateException("multiple Missed Call UpdateThreads running");
                    if (!mPendingMissedCallUpdate) {
                        mMissedCallUpdateThread = null;
                        break;
                    }
                    Log.d(TAG, "mPendingMissedCallUpdate value is " + mPendingMissedCallUpdate);
                    mPendingMissedCallUpdate = false;
                    String[] projection = new String[] { "name", "number", "date" };
                    String selection = "type = 3 AND is_read = 0";
                    Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, projection, selection, null,
                            null);
                    if (cursor == null) {
                        if (D)
                            Log.i(TAG, "MissedCallUpdateThread cursor = null");
                        mMissedCallUpdateThread = null;
                        return;
                    }
                    cursor.moveToFirst();
                    missedCallunreadCount = (byte) cursor.getCount();
                    cursor.close();
                    try {
                        if (missedCallunreadCount > 0) {
                            if (D)
                                Log.i(TAG, "missedCallunreadCount  =" + missedCallunreadCount);
                            Log.d(TAG, "unreadAlertcccDescVal[1]  =" + unreadAlertcccDescVal[1]);
                            Log.i(TAG, "unreadAlertcccDescVal::->" + unreadAlertcccDescVal[0]
                                    + "IsMissedCallUnreadAlertEnabled :->" + IsMissedCallUnreadAlertEnabled);
                            if (unreadAlertcccDescVal[0] == EnableNotification && IsMissedCallUnreadAlertEnabled == true)
                                updateUnreadAlert(AnsCategoryId.MissedCall, missedCallunreadCount);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }

                    String selection_newAlert = "type =3";
                    Cursor cursor_newAlert = getContentResolver().query(CallLog.Calls.CONTENT_URI, projection,
                            selection_newAlert, null, null);
                    if (cursor_newAlert == null) {
                        if (D)
                            Log.i(TAG, "missed call cursor_newAlert = null");
                        mMissedCallUpdateThread = null;
                        return;
                    }
                    cursor_newAlert.moveToFirst();
                    int newmissedCallCount = (byte) cursor_newAlert.getCount();
                    if (newmissedCallCount > 0) {
                        String person_name = cursor_newAlert.getString(cursor_newAlert.getColumnIndex("name"));
                        String number = null;
                        if (person_name == null) {
                            number = cursor_newAlert.getString(cursor_newAlert.getColumnIndex("number"));
                        }
                        if (missCallCount == 0) {
                            missCallCount = newmissedCallCount;
                            try {
                                targetString_missedCall = ((person_name == null) ? number : person_name);
                                if (targetString_missedCall == null) {
                                    targetString_missedCall = "You have missed call";
                                }
                                if (D)
                                    Log.i(TAG, "sending new alert with sms count = " + newAlertMissedCall
                                            + "And string =" + targetString_missedCall);
                                Log.i(TAG, "newAlertcccDescVal::->" + newAlertcccDescVal[0]
                                        + "IsMissedCallIncomingAlertEnabled :->" + IsMissedCallIncomingAlertEnabled);
                                if (newAlertcccDescVal[0] == EnableNotification && IsMissedCallIncomingAlertEnabled == true)
                                    updateNewAlert(AnsCategoryId.MissedCall, missCallCount, targetString_missedCall);
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            }
                        } else {
                            if (newmissedCallCount > missCallCount) {
                                try {
                                    targetString_missedCall = ((person_name == null) ? number : person_name);
                                    if (targetString_missedCall == null) {
                                        targetString_missedCall = "You have missed call";
                                    }
                                    newAlertMissedCall = newmissedCallCount - missCallCount;
                                    if (D)
                                        Log.i(TAG, "sending new alert with new alert count = " + newAlertMissedCall
                                                + "And string =" + targetString_missedCall);
                                    Log.i(TAG, "newAlertcccDescVal::->" + newAlertcccDescVal[0]
                                            + "IsMissedCallIncomingAlertEnabled :->" + IsMissedCallIncomingAlertEnabled);
                                    if (newAlertcccDescVal[0] == EnableNotification && IsMissedCallIncomingAlertEnabled == true)
                                        updateNewAlert(AnsCategoryId.MissedCall, newAlertMissedCall,
                                                targetString_missedCall);
                                    missCallCount = newmissedCallCount;
                                } catch (Exception e) {
                                    Log.e(TAG, e.toString());
                                }
                            }
                        }
                    }
                    cursor_newAlert.close();

                }
            }
        }
    }

    private class SMSMMSUpdateThread extends Thread {
        public SMSMMSUpdateThread() {
            super("Bluetooth SMS Update Thread");
        }

        @Override
        public void run() {

            Log.i(TAG, "SMSMMSUpdateThread started");
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            for (;;) {
                synchronized (ANPServerService.this) {
                    if (mSMSMMSUpdateThread != this)
                        throw new IllegalStateException("multiple SMSUpdateThreads running");
                    if (!mPendingSMSMMSUpdate) {
                        mSMSMMSUpdateThread = null;
                        break;
                    }
                    mPendingSMSMMSUpdate = false;
                    String[] mms_projection = new String[] { "m_id" };
                    String mms_selection = "read = 0";
                    Cursor mms_cursor = getContentResolver().query(CONTENT_URI_MMS_INBOX, mms_projection,
                            mms_selection, null, null);
                    if (mms_cursor == null) {
                        if (D)
                            Log.e(TAG, "mmscursor = null");
                        Log.i("SMS MMS Thread  unread alert", "mmscursor = null");
                        mSMSMMSUpdateThread = null;
                        return;
                    }
                    mms_cursor.moveToFirst();
                    int unreadMmsMessageCount = (byte) mms_cursor.getCount();
                    Log.i(TAG, "mms count:" + unreadMmsMessageCount);

                    mms_cursor.close();

                    String[] projection = new String[] { "person", "date", "address" };

                    String selection = "read = 0";
                    Cursor cursor = getContentResolver()
                            .query(CONTENT_URI_SMS_INBOX, projection, selection, null, null);
                    if (cursor == null) {
                        if (D)
                            Log.e(TAG, "sms cursor = null");
                        Log.i("SMS MMS Thread  unread alert", "sms cursor = null");
                        mSMSMMSUpdateThread = null;
                        return;
                    }
                    cursor.moveToFirst();
                    int unreadMessageCount = (byte) cursor.getCount();
                    Log.i("SMS MMS Thread  unread alert", "mms count:" + unreadMessageCount);
                    cursor.close();
                    newUnreadSmsMmsCount = 0;
                    try {
                        if (unreadMessageCount > 0 || unreadMmsMessageCount > 0) {
                            newUnreadSmsMmsCount = unreadMessageCount + unreadMmsMessageCount;
                            if (D)
                                Log.i(TAG, " unreadMessageCount =" + unreadMessageCount + "unread mms msg count ="
                                        + unreadMmsMessageCount);
                            Log.i("SMS MMS Thread unread alert",
                                    "calling update unread alert function with newUnreadSmsMmsCount:"
                                            + newUnreadSmsMmsCount);
                            if (mDevice == null) {
                                Log.i(TAG, "LE Device not conencted");
                            } else {
                                Log.i(TAG, "unreadAlertcccDescVal::->" + unreadAlertcccDescVal[0]
                                        + "IsSmsMmsUnreadAlertEnabled :->" + IsSmsMmsUnreadAlertEnabled);
                                if (unreadAlertcccDescVal[0] == EnableNotification && IsSmsMmsUnreadAlertEnabled == true)
                                    updateUnreadAlert(AnsCategoryId.SMS_MMS, newUnreadSmsMmsCount);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }

                    Cursor mmscursor_newAlert = getContentResolver().query(CONTENT_URI_MMS_INBOX, mms_projection, null,
                            null, null);
                    Cursor cursor_newAlert = getContentResolver().query(CONTENT_URI_SMS_INBOX, projection, null, null,
                            null);
                    Log.i(TAG, " sudhir  " + cursor_newAlert.getCount());
                    if ((cursor_newAlert.getCount() == 0) && (mmscursor_newAlert.getCount() == 0)) {
                        if (D)
                            Log.e(TAG, "sms/mms cursor_newAlert = null");
                        Log.i(" SMS MMS Thread new alert", "sms/mms cursor_newAlert = null");
                        mSMSMMSUpdateThread = null;
                        return;
                    }

                    mmscursor_newAlert.moveToFirst();
                    cursor_newAlert.moveToFirst();
                    int newMMSMessageCount = (byte) mmscursor_newAlert.getCount();
                    Log.i(" SMS MMS Thread new alert", "newMMSMessageCount" + newMMSMessageCount);
                    int newMessageCount = (byte) cursor_newAlert.getCount();
                    Log.i(" SMS MMS Thread new alert", "newSMSMessageCount" + newMessageCount);
                    if (newMessageCount > 0 || newMMSMessageCount > 0) {
              Log.i(" SMS MMS Thread new alert", "newSMSMessageCount" + newMessageCount);
                        targetString_mmsSms = null;
                        String person_name = null;
                        String address = null;
                        if (newMessageCount > 0) {
                            person_name = cursor_newAlert.getString(cursor_newAlert.getColumnIndex("person"));
                            Log.i("mms sms  new alert", "person name:" + person_name);
                            if (person_name == null) {
                                address = cursor_newAlert.getString(cursor_newAlert.getColumnIndex("address"));
                                Log.i("mms sms", "address:" + address);
                            }
                        }
                        targetString_mmsSms = ((person_name == null) ? address : person_name);
                        Log.i(" SMS MMS Thread  new alert", "target String" + targetString_mmsSms);
                        if (targetString_mmsSms == null) {
                            String mms_address = mmscursor_newAlert
                                    .getString(mmscursor_newAlert.getColumnIndex("m_id"));
                            targetString_mmsSms = mms_address;
                            Log.i(" SMS MMS Thread", "target String" + targetString_mmsSms);
                            if (targetString_mmsSms == null)
                                targetString_mmsSms = "You have a new msg";
                        }

                        String target_string_sms = trimString(targetString_mmsSms);

                        Log.i(TAG, "Target_String for sms and mms:" + target_string_sms);

                        Log.i(" SMS MMS Thread", "newUnreadSmsMmsCount:" + newUnreadSmsMmsCount + "unreadSmsMmsCount::"
                                + unreadSmsMmsCount);
                        if (newUnreadSmsMmsCount > unreadSmsMmsCount) {
                            try {
                                if (D)
                                    Log.i(TAG, "sending new alert with count = "
                                            + (newUnreadSmsMmsCount - unreadSmsMmsCount) + "And string = "
                                            + targetString_mmsSms);
                                newAlertSmsMms = newUnreadSmsMmsCount - unreadSmsMmsCount;
                                Log.i(TAG, "sending new alert with count = "
                                        + (newUnreadSmsMmsCount - unreadSmsMmsCount) + "And string = "
                                        + targetString_mmsSms);
                                if (mDevice == null) {
                                    Log.i(TAG, "No LE device conencted");
                                } else {
                                    Log.i(TAG, "newAlertcccDescVal::->" + newAlertcccDescVal[0]
                                            + "IsSmsMmsIncomingAlertEnabled :->" + IsSmsMmsIncomingAlertEnabled);

                                    if (newAlertcccDescVal[0] == EnableNotification && IsSmsMmsIncomingAlertEnabled == true)
                                        updateNewAlert(AnsCategoryId.SMS_MMS, newAlertSmsMms, target_string_sms);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            }
                        }
                        unreadSmsMmsCount = newUnreadSmsMmsCount;
                    }
                    cursor_newAlert.close();
                    mmscursor_newAlert.close();
                }
            }
        }

    }

    private String trimString(String targetString) {
        int length = targetString.length();
        if (length > 17) {
            String string = targetString.substring(0, 16);
            return string;
        } else {
            return targetString;
        }
    }

    private void updateNewAlert(int type, int count, String targetString) {
        Log.i(TAG, "update New Alert");
        if (count > 0) {
            byte newAlert = (byte) count;
            byte category = (byte) type;
            String S = targetString;
            byte[] value = null;
            byte[] attVal = null;

            if (S == null) {
                attVal = new byte[2];
                attVal[0] = category;
                attVal[1] = newAlert;
            } else {
                try {
                    value = S.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                attVal = new byte[value.length + 2];
                attVal[0] = category;
                attVal[1] = newAlert;
                for (int i = 0; i < value.length; i++)
                    attVal[i + 2] = value[i];
            }

            Log.i(TAG, "Characterestic Value Set");

            mNewAlert.setValue(attVal);

            if ((getProfileState(mDevice) == mBluetoothGattServer.STATE_CONNECTED)) {
                mBluetoothGattServer.notifyCharacteristicChanged(mDevice, mNewAlert, false);
            } else
                Log.i(TAG, "No Notification sent Gatt not connected:");
        } else {
            Log.i(TAG, "Count:" + count);
        }
    }

    private int getProfileState(BluetoothDevice mDevice) {
        return mBluetoothGattServer.getConnectionState(mDevice);
    }

    private void updateUnreadAlert(int type, int count) {
        Log.i(TAG, "in update Unread Alert");
        if (count > 0) {
            byte unreadCount = (byte) count;
            byte category = (byte) type;
            byte[] attVal = null;
            attVal = new byte[2];
            attVal[0] = category;
            attVal[1] = unreadCount;

            mUnreadAlert.setValue(attVal);
            Log.i(TAG, "Characterestic Value Set");

            if ((getProfileState(mDevice) == mBluetoothGattServer.STATE_CONNECTED)) {
                mBluetoothGattServer.notifyCharacteristicChanged(mDevice, mUnreadAlert, false);
                Log.i(TAG, " after send indication :");
            } else
                Log.i(TAG, "No Notification sent :Gatt not connected");
        } else {
            Log.i(TAG, "Count:" + count);
        }
    }

    private void showMessage(String msg) {
        Log.e(TAG, msg);
    }

    public void removeBond(BluetoothDevice device) {
        Log.d(TAG, "removeBond");
        if (mBluetoothGattServer != null) {
            mBluetoothGattServer.removeBond(device);
        }

    }

    public boolean isBLEDevice(BluetoothDevice device) {
        boolean result = false;
        Log.d(TAG, "isBLEDevice" + result);

        result = mBluetoothGattServer.isBLEDevice(device);
        Log.d(TAG, "isBLEDevice after" + result);
        return result;
    }

    public void setDeviceListHandler(Handler mHandler) {
        Log.d(TAG, "Device List Hanlder set");
        mDeviceListHanlder = mHandler;
    }

    public void setActivityHandler(Handler mHandler) {
        Log.d(TAG, "Activity Hanlder set");
        mActivityHanlder = mHandler;
    }

    public boolean checkIfBroadcastMode(byte[] scanRecord) {
        int offset = 0;
        while (offset < (scanRecord.length - 2)) {
            int len = scanRecord[offset++];
            if (len == 0)
                break;
            int type = scanRecord[offset++];
            switch (type) {
            case 0x01:

                if (len >= 2) {
                    byte flag = scanRecord[offset++];
                    if ((flag & 0x03) > 0)
                        return false;
                    else
                        return true;
                } else if (len == 1) {
                    continue;
                }
            default:
                offset += (len - 1);
                break;
            }
        }
        return false;
    }

    public void updateImmediateAlerts() {

        if (IsSmsMmsAlertIncomingNotifyImmAlertEnabled == true || IsSmsMmsAlertUnreadNotifyImmAlertEnabled == true) {
            Log.d(TAG, "updateFromSMSMMSProvider" + IsSmsMmsUnreadAlertEnabled);
            updateFromSMSMMSProvider();
        }
        if (IsIncomingCallIncomingNotifyImmAlertEnabled == true || IsIncomingCallUnreadNotifyImmAlertEnabled == true) {
            updateFromIncomingCallProvider();
        }
        if (IsMissedCallIncomingNotifyImmAlertEnabled == true || IsMissedCallUnreadNotifyImmAlertEnabled) {
            updateFromMissedCallProvider();
        }
    }
}
