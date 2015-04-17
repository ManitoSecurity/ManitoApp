/*
 * Copyright (C) 2014 The Android Open Source Project
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
 *
 * THIS FILE HAS BEEN CHANGED BY PROJECT MANITO 2014
 */

package manitosecurity.ensc40.com.manitosecurity;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity controls Bluetooth to communicate with other devices.
 */
public final class SetUpBT extends Activity {

    private static final String TAG = "SetUpBT";
    private String WifiName = "";
    private String WifiPW = "";
    private boolean WifiSecure = false;
    private boolean finishedReceiving = false;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views
    private Button mRefreshButton;
    private ImageView mRefreshIcon;
    private ImageView mSendIcon;
    private Animation slideUp, spin, slideDown;


    // Notification Handler
    Notification_Service mNotification;

    // Shared Preferences
    private SharedPreferences settings;



    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BTChat mChatService = null;
    private BTList mBTListClass = new BTList();
    private ArrayAdapter<String> mBTList;
    private ArrayAdapter<String> mNewBTList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_up_bt);
        //setHasOptionsMenu(true);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mNotification = new Notification_Service(getApplicationContext());

        mRefreshButton = (Button) findViewById(R.id.refresh_button);
        mRefreshIcon = (ImageView) findViewById(R.id.refresh_icon);
        mSendIcon = (ImageView) findViewById(R.id.send_icon);
        mRefreshIcon.setVisibility(View.INVISIBLE);
        mSendIcon.setVisibility(View.INVISIBLE);

        //Set up animation
        slideUp     = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        slideDown   = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
        spin        = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.spin);
        setAnimationEnd(slideDown, mRefreshIcon);
        setAnimationStart(slideUp, mRefreshIcon);
        setAnimationMiddle(spin, mRefreshIcon, false);
        setAnimationEnd(slideDown, mSendIcon);
        setAnimationStart(slideUp, mSendIcon);
        setAnimationMiddle(spin, mSendIcon, false);


        settings = PreferenceManager.getDefaultSharedPreferences(this);

        getActionBar().setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mBTReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mBTReceiver, filter);

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            this.finish();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupUI();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
            this.unregisterReceiver(mBTReceiver);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BTChat.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
        setupUI();

    }

    /**
     * Set up the UI and background operations
     */
    private void setupUI() {
        Log.d(TAG, "setupUI()");

        //Get the list of BT devices
        setUpButton(mRefreshButton);
        mBTList = new ArrayAdapter<String>(this,  R.layout.device_name);
        mNewBTList = new ArrayAdapter<String>(this,  R.layout.device_name);
        mBTList = mBTListClass.makeList(mBluetoothAdapter, getApplicationContext());
        ListView pairedListView = (ListView) findViewById(R.id.btlist);
        pairedListView.setOnItemClickListener(mDeviceClickListener);
        pairedListView.setAdapter(mBTList);

        // Initialize the send button with a listener that for click events
        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = v.getRootView();
                if (null != view) {
                    doBTDiscovery();
                    mRefreshButton.setText("Scanning...");
                    mRefreshButton.setBackgroundColor(getResources().getColor(R.color.medium));
                    Log.d(TAG, "refresh button pushed");
                }
            }
        });

        // Initialize the BTChat to perform bluetooth connections
        mChatService = new BTChat(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    /**
     * Set up Refresh Button
     */
    private void setUpButton(Button b){
        b.setText(R.string.scan);
        b.setTextColor(getResources().getColor(R.color.white));
        b.setBackgroundColor(getResources().getColor(R.color.dark));
    }


    /**
     * The on-click listener for all devices in the ListViews
     */
    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            if(!finishedReceiving) {
                Log.d(TAG, "can click: " + finishedReceiving);
                mBluetoothAdapter.cancelDiscovery();

                // Get the device MAC address, which is the last 17 chars in the View
                String info = ((TextView) v).getText().toString();
                String address = info.substring(info.length() - 17);

                // Create the result Intent and include the MAC address
                connectDevice(address.toString(), true);
                Toast.makeText(getApplicationContext(), "Connecting...", Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BTChat.STATE_CONNECTED) {
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BTChat to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        if (null == this) {
            return;
        }
        final ActionBar actionBar = this.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        if (null == this) {
            return;
        }
        final ActionBar actionBar = this.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    /**
     * The Handler that gets information back from the BTChat
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BTChat.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            Log.d(TAG, "CONNECTED!!! <3");
                            Log.d(TAG, "Sending wifi ssid");
                            Toast.makeText(getApplicationContext(), "sending information...", Toast.LENGTH_LONG).show();
                            setAnimationMiddle(spin, mSendIcon, false);
                            mSendIcon.startAnimation(slideUp);
                            SendWifiSSID();
                            break;
                        case BTChat.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BTChat.STATE_LISTEN:
                        case BTChat.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            setAnimationMiddle(spin, mRefreshIcon, true);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.d(TAG, "MESSAGE: " + readMessage);
                    if(readMessage.contains("E")){
                        Log.d(TAG, "ERROR");
                        Toast.makeText(getApplicationContext(), "There was an error sending wifi information", Toast.LENGTH_SHORT).show();
                    }
                    else if (readMessage.contains("S") || readMessage.contains("I") || readMessage.contains("D")){
                        SendWifiSSID();
                    }
                    else if(readMessage.contains("p") || readMessage.contains("a") || readMessage.contains("s") || readMessage.contains("w") ||
                            readMessage.contains("o") || readMessage.contains("r") || readMessage.contains("d") || readMessage.contains("e") ||
                            readMessage.contains("n") || readMessage.contains("t") || readMessage.contains("r")){
                        Log.d(TAG, "Sending wifi password");
                        setStatus(getString(R.string.title_sending, mConnectedDeviceName));
                        SendWifiPassword();
                    }
                    else{
                        if(!finishedReceiving) {
                            finishedReceiving = true;
                            Toast.makeText(getApplicationContext(), "Successfully sent information", Toast.LENGTH_SHORT).show();
                            mHandler.removeCallbacksAndMessages(null);
                            Log.d(TAG, "FINISHED C");
                            Finish();
                        }
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != this) {
                        Toast.makeText(getApplicationContext(), "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    break;
                case Constants.MESSAGE_LOST:
                    Log.d(TAG, "SetUpBTLost!!!!");
                    Log.d(TAG, "FINISHED LOST");
                    if(!finishedReceiving) {
                        Finish();
                    }
                    break;
            }
        }
    };

    private void SendWifiSSID(){
        WifiName = settings.getString("WiFiName", "");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sendMessage(WifiName+"#");
                Log.d(TAG, "sent " + WifiName);
            }
        }, 2000);

    }
    private void SendWifiPassword(){
        WifiPW = settings.getString("WiFiPassword", "");
        WifiSecure = settings.getBoolean("WiFiProtected", false);

        if(WifiSecure){
            sendMessage(WifiPW+"#");
        }
    }

    private void Finish(){
        Log.d(TAG, "FINISHED");
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        mChatService.stop();
        finish();
    }


    /**
     * Establish connection with other device
     *
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(String address, boolean secure) {
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    public void doBTDiscovery() {
        Log.d(TAG, "doBTDiscovery()");

        //clear the list of new BT devices
        mNewBTList.clear();

        // If we're already discovering, stop it
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBluetoothAdapter.startDiscovery();
        setAnimationMiddle(spin, mRefreshIcon, false);
        mRefreshIcon.startAnimation(slideUp);
    }

    /**
     * The BroadcastReceiver that listens for discovered devices
     */
    private final BroadcastReceiver mBTReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired or has been added, don't add it again
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    if(mBTList.getPosition(device.getName() + "\n" + device.getAddress()) >= 0){
                        int position = mBTList.getPosition(device.getName() + "\n" + device.getAddress());
                        String p = Integer.toString(position);
                        Log.d(TAG, "already added: " + device.getName() + " " + p);
                    } else {
                        Log.d(TAG, "added:" + device.getName());
                        mBTList.add(device.getName() + "\n" + device.getAddress());
                        mNewBTList.add(device.getName() + "\n" + device.getAddress());
                        mBTList.notifyDataSetChanged();
                    }
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "finished discovering");
                if (mNewBTList.getCount() == 0) {
                    Log.d(TAG, "added none");
                    Toast.makeText(getApplicationContext(), "No New Devices Found", Toast.LENGTH_SHORT).show();
                }
                mRefreshButton.setText("Scan for Devices");
                mRefreshButton.setBackgroundColor(getResources().getColor(R.color.dark));

                setAnimationMiddle(spin, mRefreshIcon, true);

            }
        }
    };

    private void setAnimationStart(Animation anim, final ImageView v){
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);

            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                v.startAnimation(spin);
            }


        });
    }
    //Repeat animation, then slide down if done
    private void setAnimationMiddle(Animation anim, final ImageView v, final boolean finished){
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                if(finished){
                    v.getAnimation().cancel();
                    v.startAnimation(slideDown);
                }
                else
                    v.startAnimation(spin);
            }

        });
    }

    private void setAnimationEnd(Animation anim, final ImageView v){
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.INVISIBLE);
            }
        });
    }
}