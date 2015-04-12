package manitosecurity.ensc40.com.manitosecurity;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

public class Notification_Service{
    private static final String TAG = "NOTIFICATION";
    private NotificationManager mNotificationManager;
    private Context mContext;

    public Notification_Service (Context context){
        mContext = context;
    }

    public void displayNotification() {
        Log.d(TAG, "notification");


        Notification.Builder  mBuilder = new Notification.Builder(mContext);

        mBuilder.setContentTitle("Manito Security");
        mBuilder.setContentText("Motion Detected!");
        mBuilder.setTicker("Detection");
        mBuilder.setSmallIcon(R.drawable.notification);
        mBuilder.setAutoCancel(true);

        mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        //int randomInt = (int)(1000.0 * Math.random());
        int notificationID = 1010111;
		/* notificationID allows you to update the notification later on. */
        mNotificationManager.notify(notificationID , mBuilder.build());
    }

}
