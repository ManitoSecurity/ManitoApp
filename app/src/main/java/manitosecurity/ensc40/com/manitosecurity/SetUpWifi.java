package manitosecurity.ensc40.com.manitosecurity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

public class SetUpWifi extends Activity {
    private TextView mainTex = null;
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


    StringBuilder sb = new StringBuilder();
    private static final String TAG = "SetUpWifi";

    private SharedPreferences settings;
    SharedPreferences.Editor editor;



    // Constants used for different security types
    public static final String WPA = "WPA";
    public static final String WEP = "WEP";
    public static final String WPA2 = "WPA2";
    public static final String OPEN = "Open";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "creating");
        setContentView(R.layout.set_up_wifi);
        Log.d(TAG, "set up content view");

        mainText = (TextView) findViewById(R.id.wifi_tv);
        mWifiListView = (ListView) findViewById(R.id.wifilist);
        mRefreshButton = (Button) findViewById(R.id.refresh_button);
        mRefreshIcon = (ImageView) findViewById(R.id.refresh_icon);
        mRefreshIcon.setVisibility(View.INVISIBLE);


        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiNameList = new ArrayAdapter<String>(this,  R.layout.device_name);
        mWifiList = new ArrayAdapter<ScanResult>(this,  R.layout.device_name);
        mWifiListView.setAdapter(mWifiNameList);
        mWifiListView.setOnItemClickListener(mDeviceClickListener);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        editor = settings.edit();


        //Set up animation
        slideUp     = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        slideDown   = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
        spin        = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.spin);
        setAnimationEnd(slideDown, mRefreshIcon);
        setAnimationStart(slideUp, mRefreshIcon);
        setAnimationMiddle(spin, mRefreshIcon, false);


        Log.d(TAG, "got wifi manager");

        if (!mainWifi.isWifiEnabled())
        {
            // If wifi disabled then enable it
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled",
                    Toast.LENGTH_LONG).show();

            mainWifi.setWifiEnabled(true);
        }
        receiverWifi = new WifiReceiver();
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mainWifi.startScan();
        mainText.setText("Starting Scan...");

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Refresh");
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        mainWifi.startScan();
        mainText.setText("Starting Scan");
        return super.onMenuItemSelected(featureId, item);
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
            int position = mWifiNameList.getPosition(wifiName);
            ScanResult wifiScan = mWifiList.getItem(position);
            Boolean isSecure = security(wifiScan.toString());

            Toast.makeText(getApplicationContext(), wifiName + ": " + "Protected: " + isSecure, Toast.LENGTH_SHORT).show();
            editor.putString("WiFiName", wifiName).commit();
            editor.putBoolean("WiFiProtected", isSecure).commit();
        }
    };

    public Boolean security(String cap){
        if (cap.toLowerCase().contains(WEP.toLowerCase())){
            return true;}

        else if (cap.toLowerCase().contains(WPA2.toLowerCase())){
            return true;}

        else if (cap.toLowerCase().contains(WPA.toLowerCase())){
            return true;}

        else
            return false;

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

            /*
            for(int i = 0; i < wifiList.size(); i++){
                Log.d(TAG, "adding this:" + wifiList.get(i).toString());
                //mWifiList.add(wifiList.get(i).toString());

                sb.append(new Integer(i+1).toString() + ". ");
                sb.append((wifiList.get(i)).toString());
                sb.append("\n\n");
            }
            */


            mainText.setText(sb);
        }

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
