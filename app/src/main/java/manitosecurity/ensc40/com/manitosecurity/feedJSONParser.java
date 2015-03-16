package manitosecurity.ensc40.com.manitosecurity;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Collin on 3/15/2015.
 * based on tutorial by George Mathew at http://wptrafficanalyzer.in/blog/android-lazy-loading-images-and-text-in-listview-from-http-json-data/
 */
public class feedJSONParser {

    String TAG = "feedJSONParser";

    // Receives a JSONObject and returns a list
    public List<HashMap<String, Object>> parse(JSONObject jObject) {

        JSONArray jEvents = null;
        try {
            jEvents = jObject.getJSONArray("events");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "parse, about to return " + jObject.toString());

        // Invoking getEvent with the array of json object
        // where each json object represent a event
        return getEvents(jEvents);
    }

    private List<HashMap<String, Object>> getEvents(JSONArray jEvents) {
        int eventCount = jEvents.length();
        List<HashMap<String, Object>> eventList = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> event = null;

        Log.d(TAG, "in getEvents");

        // Taking each event, parses and adds to list object
        for (int i = 0; i < eventCount; i++) {
            try {
                // Call getEvent with event JSON object to parse the event
                event = getEvent((JSONObject) jEvents.get(i));
                eventList.add(event);
                Log.d(TAG, "adding event");
            } catch (JSONException e) {
                Log.d(TAG, "in exception for hashmap");
                e.printStackTrace();
            }
        }

        return eventList;
    }

    // Parsing the Event JSON object
    private HashMap<String, Object> getEvent(JSONObject jEvent) {
        Log.d(TAG, "getEvent");


        HashMap<String, Object> event = new HashMap<String, Object>();
        String  m_name  = "";
        String  m_time  = "";
        String  b_arm   = "";
        String  m_arm   = "";
        String  m_home  = "";
        String  b_alert = "";
        String  m_alert = "";

        try {
            //m_name = jEvent.getString("name");
            m_time   = jEvent.getString("timestamp");
            m_time = m_time.substring(11, 19);
            b_arm    = jEvent.getString("armed");
            Log.d(TAG, "b_arm: " + b_arm);
            if(b_arm.equals("T")){
                m_arm = "Armed";
            } else{
                m_arm = "Disarmed";
            }
            //m_home = jEvent.getString("home");
            b_alert  = jEvent.getString("alert");
            if(b_alert.equals("T")){
                m_alert = "Armed";
            } else{
                m_alert = "Disarmed";
            }

            //event.put("name", m_name);
            event.put("timestamp", m_time);
            event.put("armed", m_arm);
            //event.put("home", m_home);
            event.put("alert", m_alert);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "getEvent about to return");

        return event;
    }
}