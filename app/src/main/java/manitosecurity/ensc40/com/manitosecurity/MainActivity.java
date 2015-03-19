package manitosecurity.ensc40.com.manitosecurity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity {

    private ImageButton imageState;
    private ImageView emptyFeed;
    private ListView mListView;
    private SharedPreferences settings;
    private SwipeRefreshLayout swipeLayout;
    private DownloadTask downloadTask = new DownloadTask();

    SharedPreferences.Editor editor;
    String strUrl = "";
    String TAG = "MAINTAG";

    /**
     * Member object for the chat services
     */
    private BTChat mChatService = null;
    private StringBuffer mOutStringBuffer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //GET SHARED PREFERENCES and SET UP EDITOR
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        editor = settings.edit();

        //Set up finished
        //if(!settings.getBoolean("setUp", false)) {setUpFinished();}
        mChatService = new BTChat(this, mHandler);
        mOutStringBuffer = new StringBuffer("");

        setUpUI();


        strUrl = "https://data.sparkfun.com/output/5JZO9K83dRU0KlA39EGZ.json";
        //strUrl = "https://data.sparkfun.com/output/YGbWzd9amwuzd1KwJjDK.json";

        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        });

        // Starting the download process
        downloadTask.execute(strUrl);
    }

    @Override
    protected void onResume(){
        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settings = new Intent(getApplicationContext(), SetUp.class);
                startActivity(settings);
                return true;
            case R.id.developer_setting:
                Intent developer = new Intent(getApplicationContext(), DeveloperChat.class);
                startActivity(developer);
                return true;
            case R.id.wifi_detection:
                Intent wifi = new Intent(getApplicationContext(), SetUpWifi.class);
                startActivity(wifi);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpFinished(){
        editor.putBoolean("setUp", true).commit();
    }

    private void setUpUI(){
        Log.d(TAG, "setting up ui");
        mListView = (ListView) findViewById(R.id.lv_events);
        emptyFeed = (ImageView) findViewById(R.id.empty_graphic);
        emptyFeed.setVisibility(View.INVISIBLE);
        imageState = (ImageButton) findViewById(R.id.stateImage);

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                downloadTask = new DownloadTask();
                strUrl = "https://data.sparkfun.com/output/5JZO9K83dRU0KlA39EGZ.json";
                downloadTask.execute(strUrl);
            }
        });
        swipeLayout.setColorScheme(
                R.color.green,
                R.color.orange,
                R.color.dark);

        if (!settings.getBoolean("armState", false)){		//if setting is off, button should be off
            imageState.setImageResource(R.drawable.button_off);
        }

        imageState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = settings.edit();
                if(settings.getBoolean("armState", false) == true){			//if armed -> disarm
                    imageState.setImageResource(R.drawable.button_off);
                    editor.putBoolean("armState", false).commit();
                    sendMessage("D");
                }

                else{   //disarm -> arm
                    imageState.setImageResource(R.drawable.button_on);
                    editor.putBoolean("armState", true).commit();
                    sendMessage("A");
                }
            }
        });


        Log.d(TAG, "set up ui");
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
                emptyFeed.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.INVISIBLE);
            }
            else{
                emptyFeed.setVisibility(View.INVISIBLE);
                mListView.setVisibility(View.VISIBLE);

                // The parsing of the xml data is done in a non-ui thread
                ListViewLoaderTask listViewLoaderTask = new ListViewLoaderTask();

                // Start parsing xml data
                listViewLoaderTask.execute(result);
            }

            swipeLayout.setRefreshing(false);

        }
    }


    /** AsyncTask to parse json data and load ListView*/
    private class ListViewLoaderTask extends AsyncTask<String, Void, SimpleAdapter>{

        JSONObject jObject = null;
        // Doing the parsing of xml data in a non-ui thread
        @Override
        protected SimpleAdapter doInBackground(String... strJson) {
            try{
                jObject = new JSONObject(strJson[0]);
                Log.d(TAG, "made the jObject");
                feedJSONParser eventJsonParser = new feedJSONParser();
                eventJsonParser.parse(jObject);
            }catch(Exception e){
                Log.d("JSON Exception1",e.toString());
            }

            // Instantiating json parser class
            feedJSONParser eventJsonParser = new feedJSONParser();

            // A list object to store the parsed events list
            List<HashMap<String, Object>> events = null;

            try{
                // Getting the parsed data as a List construct
                events = eventJsonParser.parse(jObject);
            }catch(Exception e){
                Log.d("Exception",e.toString());
            }

            // Keys used in Hashmap
            //String[] from = { "name","timestamp","armed", "home", "alert"};
            String[] from = { "timestamp","armed", "armed_img", "date", "separate"};

            // Ids of views in listview_layout
            //int[] to = { R.id.contact_name_text, R.id.contact_time_text, R.id.contact_armed_text, R.id.contact_home_text};
            int[] to = { R.id.contact_time_text, R.id.contact_armed_text, R.id.contact_armed_picture, R.id.date};

            // Instantiating an adapter to store each items
            // R.layout.listview_layout defines the layout of each item
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), events, R.layout.feed_layout, from, to);



            return adapter;
        }

        /** Invoked by the Android on "doInBackground" is executed*/
        @Override
        protected void onPostExecute(SimpleAdapter adapter) {

            // Setting adapter for the listview
            mListView.setAdapter(adapter);

            for(int i=0;i<adapter.getCount();i++){
                HashMap<String, Object> hm = (HashMap<String, Object>) adapter.getItem(i);
                String imgUrl = (String) hm.get("flag_path");

            }
        }
    }


    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != this) {
                        Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BTChat.STATE_CONNECTED) {
            Toast.makeText(this, "Not Connected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BTChat to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }



}
