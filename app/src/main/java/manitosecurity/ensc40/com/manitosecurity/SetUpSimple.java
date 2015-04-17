package manitosecurity.ensc40.com.manitosecurity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class SetUpSimple extends ActionBarActivity {

    private Intent set_up_wifi = null;
    private Intent main_activity = null;
    static final int GET_WIFI = 2;
    private String TAG = "SETUP";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);
        set_up_wifi = new Intent(getApplicationContext(), SetUpWifi.class);
        main_activity = new Intent(getApplicationContext(), MainActivity.class);

        getWIFI();
    }

    private void getWIFI(){
        Log.d(TAG, "getBTandWIFI");
        startActivityForResult(set_up_wifi, GET_WIFI);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_WIFI) {
            if(resultCode == RESULT_OK){
                startActivity(main_activity);
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
