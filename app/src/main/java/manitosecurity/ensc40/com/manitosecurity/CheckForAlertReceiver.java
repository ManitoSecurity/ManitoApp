package manitosecurity.ensc40.com.manitosecurity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Collin on 4/9/2015.
 */
public class CheckForAlertReceiver extends BroadcastReceiver {

    private String TAG = "AlarmReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent minuteUpdater = new Intent(context, CheckForAlertService.class);
        context.startService(minuteUpdater);
        Log.d(TAG, "RECEIVER");
    }
}
