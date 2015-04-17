package manitosecurity.ensc40.com.manitosecurity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

public class CheckWifi extends BroadcastReceiver {

    private String log = "CheckWifi";
    public Context mcontext;
    public Handler mhandler;

    private FeedHandler fh = new FeedHandler(mcontext, mhandler);


    public void onReceive(final Context context, final Intent intent) {
        mcontext = context;

        if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo networkInfo =
                    intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(networkInfo.isConnected()) {
                // Wifi is connected
                Log.d(log, "Wifi is connected: " + String.valueOf(networkInfo));
                fh.updateFeed("9364467121", "F", "F", "F", "F");
            }
        } else if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            NetworkInfo networkInfo =
                    intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI &&
                    ! networkInfo.isConnected() && networkInfo.getState() != NetworkInfo.State.CONNECTING
                    && networkInfo.getState() != NetworkInfo.State.SUSPENDED) {
                // Wifi is disconnected
                Log.d(log, "Wifi is disconnected: " + String.valueOf(networkInfo));
                fh.updateFeed("9364467121", "T", "F", "T", "F");
            }
        }
    }

}
