package manitosecurity.ensc40.com.manitosecurity;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Collin on 4/9/2015.
 */
public class CheckForAlertService extends IntentService {

    private String TAG = "CheckForAlertService";

    public CheckForAlertService(){
        super("CheckForAlertSERVICE");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "About to execute MyTask");
        Context ctx = getApplicationContext();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = settings.edit();

        new CheckForAlert().check(ctx, settings, editor);
    }
}
