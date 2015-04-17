package manitosecurity.ensc40.com.manitosecurity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class SetUpSimple extends Activity {

    private Intent set_up_bt = null;
    private Intent set_up_wifi = null;
    private Intent main_activity = null;
    static final int GET_BT = 1;
    static final int GET_WIFI = 2;
    private String TAG = "SETUP";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "IN SETUP");
        setContentView(R.layout.activity_set_up);
        set_up_bt = new Intent(getApplicationContext(), SetUpBTSimple.class);
        set_up_wifi = new Intent(getApplicationContext(), SetUpWifiSimple.class);
        main_activity = new Intent(getApplicationContext(), MainActivity.class);

        getBTandWIFI();
    }

    private void getBTandWIFI(){
        Log.d(TAG, "getBTandWIFI");
        startActivityForResult(set_up_wifi, GET_WIFI);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == GET_BT) {
            if(resultCode == RESULT_OK){
                startActivity(main_activity);
            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Unable to connect to BlueTooth", Toast.LENGTH_SHORT).show();
                Intent return_intent = new Intent(this, SetUpWifi.class);
                startActivity(return_intent);            }
        }

        if (requestCode == GET_WIFI) {
            if(resultCode == RESULT_OK){
                startActivityForResult(set_up_bt, GET_BT);
            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Unable to connect to Wifi", Toast.LENGTH_SHORT).show();
                Intent return_intent = new Intent(this, WelcomeScreen.class);
                startActivity(return_intent);
            }
        }
    }//onActivityResult

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_set_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
