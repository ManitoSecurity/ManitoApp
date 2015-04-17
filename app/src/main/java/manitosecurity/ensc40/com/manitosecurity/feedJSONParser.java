package manitosecurity.ensc40.com.manitosecurity;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by Collin on 3/15/2015.
 * based on tutorial by George Mathew at http://wptrafficanalyzer.in/blog/android-lazy-loading-images-and-text-in-listview-from-http-json-data/
 */
public class feedJSONParser {

    String TAG = "feedJSONParser";
    private Context mContext;
    private String mPhoneNumber;
    private String manitoNumber = "0008675309";
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;


    public feedJSONParser(Context c){
        mContext = c;
        mPhoneNumber = getPhoneNumber();
        settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = settings.edit();
    }

    // Receives a JSONObject and returns a list
    public List<HashMap<String, Object>> parse(JSONObject jObject) {

        JSONArray jEvents = null;
        try {
            jEvents = jObject.getJSONArray("events");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Invoking getEvent with the array of json object
        // where each json object represent a event
        return getEvents(jEvents);
    }

    private List<HashMap<String, Object>> getEvents(JSONArray jEvents) {
        int eventCount = jEvents.length();
        List<HashMap<String, Object>> eventList = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> event = null;
        String s_latestArm = "";
        Boolean b_latestArm = false;

        // Taking each event, parses and adds to list object
        for (int i = 0; i < eventCount; i++) {
            try {
                // Call getEvent with event JSON object to parse the event
                event = getEvent((JSONObject) jEvents.get(i));
                eventList.add(event);
                //Log.d(TAG, "adding event");
                if(i == 0){
                    s_latestArm = event.get("armed").toString();
                    Log.d(TAG, "latest arm: " + s_latestArm);
                    if(s_latestArm.equals("Armed")){
                        b_latestArm = true;
                    }else{
                        b_latestArm = false;
                    }
                    editor.putBoolean("armState", b_latestArm).commit();
                }
            } catch (JSONException e) {
                Log.d(TAG, "in exception for hashmap ");
                e.printStackTrace();
            }
        }

        return eventList;
    }

    // Parsing the Event JSON object
    private HashMap<String, Object> getEvent(JSONObject jEvent) {
        //Log.d(TAG, "getEvent");


        HashMap<String, Object> event = new HashMap<String, Object>();
        String  m_name  = "";
        String  d_name  = "";
        String  m_time_stamp  = "";
        String  m_time  = "";
        String  b_arm   = "";
        String  m_arm   = "";
        String  b_away  = "";
        String  m_away  = "";
        String  b_sleep  = "";
        String  b_alert = "";
        String  m_alert = "";
        String  d_arm   = null;
        String  d_away  = null;
        String  m_date  = "";
        String  m_month  = "";
        Boolean separate = false;
        String  m_lastDate = "";

        try {
            m_name = jEvent.getString("phone");
            //long contactid = Long.parseLong(fetchContactIdFromPhoneNumber(m_name));
            m_name = getContactName(mContext, m_name);
            m_time_stamp   = jEvent.getString("timestamp");
            m_time         = m_time_stamp.substring(11, 16);
            m_date         = m_time_stamp.substring(8, 10);
            m_month        = m_time_stamp.substring(5, 7);
            //Log.d(TAG, m_date + " " + m_month);

            b_away         = jEvent.getString("away");
            b_sleep        = jEvent.getString("sleep");

            String[] time = {m_time, m_date};
            time = to_CivilianTime(time);
            m_time = time[0];
            m_date = time[1];
            m_month = getMonth(Integer.parseInt(m_month));
            //Log.d(TAG, m_date + " " + m_month);


            m_date = m_month + " " + m_date;

            if(m_lastDate == m_date){
                separate = false;
            }
            else{
                separate = true;
            }

            b_arm    = jEvent.getString("armed");
            if(b_arm.equals("T")){
                m_arm = "Armed";
                d_arm = String.valueOf(R.drawable.armed_on);
            } else{
                m_arm = "Disarmed";
                d_arm = String.valueOf(R.drawable.armed_off);
            }

            if(b_away.equals("T")){
                d_away = String.valueOf(R.drawable.home_off);
                m_away = "Away";
            } else{
                m_away = "Home";
                d_away = String.valueOf(R.drawable.home_on);
            }

            b_alert  = jEvent.getString("alert");
            if(b_alert.equals("T")){
                m_alert = "Armed";
            } else{
                m_alert = "Disarmed";
            }

            if(m_name.equals("Manito")){
                d_name = String.valueOf(R.drawable.manito_contact);
                d_away = String.valueOf(R.drawable.alert);
                m_away = "Alert";

            }else if(m_name.equals("You")){
                d_name = String.valueOf(R.drawable.owner_contact);
            } else{
                d_name = String.valueOf(R.drawable.default_contact);
            }

            //event.put("name", m_name);
            event.put("timestamp", m_time);
            event.put("armed", m_arm);
            //event.put("home", m_home);
            event.put("alert", m_alert);
            event.put("armed_img", d_arm);
            event.put("date", m_date);
            event.put("separate", separate);
            event.put("home_img", d_away);
            event.put("home", m_away);
            event.put("name", m_name);
            event.put("name_image", d_name);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Log.d(TAG, "getEvent about to return");

        return event;
    }

    private String[] to_CivilianTime(String[] time){
        Integer i_day = Integer.parseInt(time[1]);
        StringBuilder time_finished = new StringBuilder();
        String s_hours = time[0].substring(0, 2);

        TimeZone tZone = TimeZone.getDefault();


        long daylightSaving = tZone.getDSTSavings();
        daylightSaving = TimeUnit.HOURS.convert(daylightSaving, TimeUnit.MILLISECONDS);

        long offset = tZone.getRawOffset();
        offset = TimeUnit.HOURS.convert(offset, TimeUnit.MILLISECONDS);

        int i_hours = Integer.parseInt(s_hours);

        i_hours += offset;
        i_hours += daylightSaving;


        if(i_hours < 0){
            i_hours += 24;
            i_day = Integer.parseInt(time[1]) - 1;
        }

        if(i_hours >= 24){
            i_hours -= 24;
        }

        if(i_hours > 12){
            i_hours -= 12;
            if(i_hours < 10){
                time_finished.append("0");}
            time_finished.append(i_hours);
            time_finished.append(":");
            time_finished.append(time[0].substring(3));
            time_finished.append(" pm");
        }else{
            time_finished.append(i_hours);
            time_finished.append(":");
            time_finished.append(time[0].substring(3));
            time_finished.append(" am");
        }

        time[1] = i_day.toString();
        //Log.d(TAG, "!!!!!!!" + time[0] + " " + time[1]);

        time[0] = time_finished.toString();

        return time;
    }

    private String getContactName(Context context, String number) {
        //Log.d(TAG, "Trying to find it" + number);

        String name = null;
        // define the columns I want the query to return
        String[] projection = new String[] {
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup._ID,
                ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI};

        // encode the phone number and build the filter URI
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        // query time
        Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null);

        if(cursor != null) {
            if (cursor.moveToFirst()) {
                name =      cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                String a_name[] = name.split(" ", 2);
                name = a_name[0] + " " + a_name[1].substring(0,1) + ".";
            } else {
                //Log.d(TAG, "Contact Not Found @ " + number);
                if(number.equals(mPhoneNumber)){
                    name = "You";
                }
                else if(number.equals(manitoNumber)){
                    name = "Manito";
                }
                else{
                    name = number;
                }
            }
            cursor.close();
        }

        return name;
    }

    private String getPhoneNumber(){
        TelephonyManager tMgr = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String mPhoneNumber = tMgr.getLine1Number();

        return mPhoneNumber;
    }

    public String getMonth(int month) {
        return new DateFormatSymbols().getMonths()[month-1];
    }
}