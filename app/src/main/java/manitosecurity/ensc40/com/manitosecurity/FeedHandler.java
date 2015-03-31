package manitosecurity.ensc40.com.manitosecurity;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Collin on 3/30/2015.
 */
public class FeedHandler {

    static String TAG = "FeedHandler";
    private String mPhoneNumber, mArmed, mAlert = "";
    private String privateKey = "7BMDzNyXeAf0Kl25JoW1";
    private String publicKey = "5JZO9K83dRU0KlA39EGZ";
    private String path = "http://data.sparkfun.com/input/" + publicKey;
    private List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);


    public void updateFeedASYNC(){
        nameValuePairs.clear();

        nameValuePairs.add(new BasicNameValuePair("phone", mPhoneNumber));
        nameValuePairs.add(new BasicNameValuePair("alert", mAlert));
        nameValuePairs.add(new BasicNameValuePair("armed", mArmed));

        try{
            Log.d(TAG, "Trying makeRequest");
            makeRequest(path, nameValuePairs);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void updateFeed(String phoneNumber, String armed, String alert){
        mPhoneNumber = phoneNumber;
        mArmed = armed;
        mAlert = alert;

        Log.d(TAG, "updateFEED:" + mAlert + mPhoneNumber);

        new HttpAsyncTask().execute();
    }

    public void makeRequest(String path, List<NameValuePair> params) throws Exception{

        InputStream inputStream = null;
        String result = "";

        //instantiates httpclient to make request
        DefaultHttpClient httpclient = new DefaultHttpClient();

        //url with the post data
        HttpPost httpost = new HttpPost(path);

        //sets the post request as the resulting string
        httpost.setEntity(new UrlEncodedFormEntity(params));


        httpost.addHeader("Phant-Private-Key", privateKey);

        //Handles what is returned from the page
        HttpResponse httpResponse = httpclient.execute(httpost);

        //receive response as inputStream
        inputStream = httpResponse.getEntity().getContent();

        //convert inputstream to string
        if(inputStream != null)
            result = convertInputStreamToString(inputStream);
        else
            result = "Did not work!";

        Log.d(TAG, result);
    }


    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            updateFeedASYNC();
            return "";
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
        }
    }

}
