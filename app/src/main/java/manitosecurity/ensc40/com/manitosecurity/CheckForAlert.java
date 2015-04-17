package manitosecurity.ensc40.com.manitosecurity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Collin on 4/9/2015.
 */
public class CheckForAlert {

    private String TAG = "CheckForAlerts";
    private DownloadTask downloadTask = new DownloadTask();
    private String strUrl = "https://data.sparkfun.com/output/5JZO9K83dRU0KlA39EGZ.json";
    private Context mContext;
    private FindAlerts fAlerts = new FindAlerts();
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;


    public void check(Context c, SharedPreferences s, SharedPreferences.Editor e){
        mContext = c;
        downloadTask.execute(strUrl);
        settings = s;
        editor = e;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
            return "";
        }finally{
            iStream.close();
        }

        return "{\"events\":" + data + "}";
    }

    // Receives a JSONObject and returns a list
    public void parse(JSONObject jObject) {

        JSONArray jEvents = null;
        try {
            jEvents = jObject.getJSONArray("events");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Invoking getEvent with the array of json object
        // where each json object represent a event
        checkEvents(jEvents);
    }

    /** AsyncTask to download json data*/
    private class DownloadTask extends AsyncTask<String, Integer, String> {
        String data = null;
        @Override
        protected String doInBackground(String... url) {
            try{
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "FINISHED DOWNLOADING");

            if(result == null){
                Log.d(TAG, "EMPTY FEED");
            }
            else{
                fAlerts.execute(result);
            }
        }
    }



    /** AsyncTask to parse json data and load ListView*/
    private class FindAlerts extends AsyncTask<String, Void, Boolean>{

        JSONObject jObject = null;
        // Doing the parsing of xml data in a non-ui thread
        @Override
        protected Boolean doInBackground(String... strJson) {
            try{
                jObject = new JSONObject(strJson[0]);
                Log.d(TAG, "made the jObject");
                parse(jObject);
            }catch(Exception e){
                Log.d("JSON Exception1",e.toString());
            }

            return true;
        }
    }

    private void checkEvents(JSONArray jEvents) {
        int eventCount = jEvents.length();

        // Taking each event, parses and adds to list object
        for (int i = 0; i < eventCount; i++) {
            try {
                checkEvent((JSONObject) jEvents.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkEvent(JSONObject jEvent) {
        String  b_alert = "";
        String  m_time = "";

        long mlatestAlert = settings.getLong("lastAlert", 123456);
        long currenttime;

        try {
            b_alert  = jEvent.getString("alert");
            m_time  = jEvent.getString("timestamp");
            currenttime = dateToMilliseconds(m_time);

            Log.d(TAG, "last " + mlatestAlert + " new " + currenttime + " b_alert" + b_alert);

            boolean isNewer = currenttime > (mlatestAlert + 1000);


            if(b_alert.equals("T") && isNewer){
                editor.putLong("lastAlert", currenttime).commit();
                if(settings.getBoolean("armState", false)) {
                    Notification_Service mNotificationService = new Notification_Service(mContext);
                    mNotificationService.displayNotification();
                }
                Log.d(TAG, "ALERT " + mlatestAlert);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private long dateToMilliseconds(String date){
        Calendar calendar = Calendar.getInstance();
        Date oldDate = new Date();

        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(5, 7));
        int day = Integer.parseInt(date.substring(8, 10));
        int hour = Integer.parseInt(date.substring(11, 13));
        int minute = Integer.parseInt(date.substring(14, 16));
        int seconds = Integer.parseInt(date.substring(17, 19));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

        String oldTime = year + "." + month + "." + day + "." + hour + "." + minute + "." + seconds;
        Log.d(TAG, oldTime);

        try {
            oldDate = formatter.parse(oldTime);
        } catch(Exception e){
            Log.d("converting time", e.toString());
        }

        calendar.set(year, month, day, hour, minute, seconds);

        long time = oldDate.getTime();
        Log.d(TAG, "--------------------------------" + time);

        return time;
    }

}
