package manitosecurity.ensc40.com.manitosecurity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;


public class WelcomeScreen extends Activity {

    private SharedPreferences settings;
    private Button setUp, website, help;
    private Animation slideUp, slideRight, slideDown;
    String TAG = "MAINTAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_screen);
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        getActionBar().hide();
        if (settings.getBoolean("setUp", false)){		//if already setup, go to main activity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

        slideUp =       AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        slideRight =    AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_right);
        slideDown =     AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);

        slideDown.setFillAfter(true);
        slideRight.setFillAfter(true);

        setUpUI();

    }

    @Override
    protected void onResume() {
        super.onResume();
        animateButtons();
    }

    private void setUpUI(){
        setUp   = (Button) findViewById(R.id.set_up_button);
        website = (Button) findViewById(R.id.website_button);
        help    = (Button) findViewById(R.id.help_button);

        setButtonListeners();;

        animateButtons();
    }

    private void animateButtons(){
        setUp.startAnimation(slideUp);
        website.startAnimation(slideUp);
        help.startAnimation(slideUp);
    }

    private void setButtonListeners(){
        setUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PickSetUp.class);
                setAnimationEnd(slideDown, intent);
                setUp.bringToFront();
                help.startAnimation(slideDown);
                website.startAnimation(slideDown);
            }
        });

        website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Uri uri = Uri.parse("http://www.manitosecurity.com/");
                Intent webintent = new Intent(Intent.ACTION_VIEW, uri);
                setAnimationEnd(slideDown, webintent);
                website.bringToFront();
                setUp.startAnimation(slideDown);
                help.startAnimation(slideDown);
            }
        });

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://github.com/ManitoSecurity");
                Intent helpintent = new Intent(Intent.ACTION_VIEW, uri);
                setAnimationEnd(slideDown, helpintent);
                help.bringToFront();
                setUp.startAnimation(slideDown);
                website.startAnimation(slideDown);
            }
        });

    }

    private void setAnimationEnd(Animation anim, final Intent intent){
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationStart(Animation animation) {}


            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(intent);
            }

        });
    }
}