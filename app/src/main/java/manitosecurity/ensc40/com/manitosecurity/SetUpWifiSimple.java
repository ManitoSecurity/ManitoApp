package manitosecurity.ensc40.com.manitosecurity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
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

import java.util.List;


public class SetUpWifiSimple extends Activity {
    private Button mRefreshButton;
    private WifiManager mainWifi = null;
    private WifiReceiver receiverWifi = null;
    private List<ScanResult> wifiList = null;
    private TextView mainText = null;
    private ListView mWifiListView = null;
    private ArrayAdapter<String> mWifiNameList;
    private ArrayAdapter<ScanResult> mWifiList;
    private Animation slideUp, spin, slideDown;
    private ImageView mRefreshIcon;

    private static final String TAG = "SetUpWifi";

    private SharedPreferences settings;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "creating");
        setContentView(R.layout.activity_set_up_wifi_simple);
        Log.d(TAG, "set up content view");

        mainText = (TextView) findViewById(R.id.wifi_tv);
        mWifiListView = (ListView) findViewById(R.id.wifilist);
        mRefreshButton = (Button) findViewById(R.id.refresh_button);
        mRefreshIcon = (ImageView) findViewById(R.id.refresh_icon);
        mRefreshIcon.setVisibility(View.INVISIBLE);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        editor = settings.edit();


        //Set up animation
        slideUp     = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        slideDown   = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
        spin        = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.spin);
        setAnimationEnd(slideDown, mRefreshIcon);
        setAnimationStart(slideUp, mRefreshIcon);
        setAnimationMiddle(spin, mRefreshIcon, false);

        getActionBar().setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        getActionBar().setTitle("");

        Log.d(TAG, "got wifi manager");
        setupUI();
        if (!mainWifi.isWifiEnabled())
        {
            // If wifi disabled then enable it
            Toast.makeText(getApplicationContext(), "Wifi is disabled. Enabling Wifi",
                    Toast.LENGTH_LONG).show();

            mainWifi.setWifiEnabled(true);
        }
        receiverWifi = new WifiReceiver();
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        //mainWifi.startScan();
    }


    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            String wifiName = ((TextView) v).getText().toString();
            editor.putString("WiFiName", wifiName).commit();

            Finish();


        }
    };

    private void Finish(){
        Log.d(TAG, "FINISHED");
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        unregisterReceiver(receiverWifi);
        finish();
    }

    // Broadcast receiver class called its receive method
    // when number of wifi connections changed
    class WifiReceiver extends BroadcastReceiver {
        // This method call when number of wifi connections changed
        public void onReceive(Context c, Intent intent) {

            wifiList = mainWifi.getScanResults();

            for(ScanResult wifi : wifiList){
                if(wifi.SSID.toString() != "") {
                    if(mWifiNameList.getPosition(wifi.SSID.toString()) >= 0){
                        Log.d(TAG, "already added:" + wifi.SSID.toString());
                    }
                    else {
                        Log.d(TAG, "adding: " + wifi.SSID.toString());
                        mWifiNameList.add(wifi.SSID.toString());
                        mWifiList.add(wifi);
                        mWifiNameList.notifyDataSetChanged();
                    }
                }
            }

            mRefreshButton.setText("Scan for Networks");
            mRefreshButton.setBackgroundColor(getResources().getColor(R.color.dark));
            setAnimationMiddle(spin, mRefreshIcon, true);

        }

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
     * Start device discover with the BluetoothAdapter
     */
    public void doWiFiDiscovery() {
        Log.d(TAG, "doBTDiscovery()");

        // Request discover from BluetoothAdapter
        mainWifi.startScan();
        setAnimationMiddle(spin, mRefreshIcon, false);
        mRefreshIcon.startAnimation(slideUp);
    }

    private void setupUI() {
        Log.d(TAG, "setupUI()");

        //Get the list of BT devices
        setUpButton(mRefreshButton);
        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiNameList = new ArrayAdapter<String>(this,  R.layout.device_name);
        mWifiList = new ArrayAdapter<ScanResult>(this,  R.layout.device_name);
        mWifiListView.setAdapter(mWifiNameList);
        mWifiListView.setOnItemClickListener(mDeviceClickListener);

        mRefreshButton.setText("Scanning...");
        mRefreshButton.setBackgroundColor(getResources().getColor(R.color.medium));


        // Initialize the send button with a listener that for click events
        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = v.getRootView();
                if (null != view) {
                    doWiFiDiscovery();
                    mRefreshButton.setText("Scanning...");
                    mRefreshButton.setBackgroundColor(getResources().getColor(R.color.medium));
                    Log.d(TAG, "refresh button pushed");
                }
            }
        });
        doWiFiDiscovery();
    }

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
