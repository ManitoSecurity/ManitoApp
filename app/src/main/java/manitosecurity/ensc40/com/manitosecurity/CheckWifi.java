package manitosecurity.ensc40.com.manitosecurity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

public class CheckWifi extends BroadcastReceiver {

    private String log = "CheckWifi";
    public Context mcontext;
    public Handler mhandler;
    private SharedPreferences settings;
    private String WifiName;
    private String mPhoneNumber;
    private String lastConnectedSSID;
    private SharedPreferences.Editor editor;

    private FeedHandler fh = new FeedHandler(mcontext, mhandler);


    public void onReceive(final Context context, final Intent intent) {
        settings = PreferenceManager.getDefaultSharedPreferences(context);
        WifiName = settings.getString("WiFiName", "");
        mPhoneNumber = settings.getString("PhoneNumber", "");
        lastConnectedSSID = settings.getString("LastConnectedSSID", "");
        editor = settings.edit();
        //Log.d(log, "Wifi shared preference: " + WifiName);
        Log.d(log, "lastConnectedSSID ONRECEIVE----------" + lastConnectedSSID);


        if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo networkInfo =
                    intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            WifiManager mainWifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo currentWifi = mainWifi.getConnectionInfo();
            String currentWifiName = currentWifi.getSSID();
            currentWifiName = currentWifiName.replace("\"", "");
            if(networkInfo.isConnected()) {
                lastConnectedSSID = currentWifiName;
                editor.putString("LastConnectedSSID", lastConnectedSSID).commit();
                Log.d(log, "new lastConnectedSSID " + lastConnectedSSID);
                // Wifi is connected
                if(currentWifiName.equals(WifiName)) {
                    Log.d(log, "Wifi is connected: " + String.valueOf(networkInfo));
                    fh.updateFeed(mPhoneNumber, "F", "F", "F", "F");
                }
            }
        } else if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            NetworkInfo networkInfo =
                    intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI &&
                    ! networkInfo.isConnected() && networkInfo.getState() != NetworkInfo.State.CONNECTING
                    && networkInfo.getState() != NetworkInfo.State.SUSPENDED
                    && lastConnectedSSID.equals(WifiName)) {
                // Wifi is disconnected
                Log.d(log, "Wifi is disconnected: " + String.valueOf(networkInfo));
                fh.updateFeed(mPhoneNumber, "T", "F", "T", "F");
            }
        }
    }


}
