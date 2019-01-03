package in.blrobotics.blaengarrobotics;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.PreferenceManager;


public class SplashActivity extends AppCompatActivity {
    MySQLConnection conn;

    @Override
    protected void onStart() {
        super.onStart();
        /* connecting to database */
        conn = new MySQLConnection(SplashActivity.this, getString(R.string.dbConnAddress), getString(R.string.dbName), getString(R.string.dbUserName), getString(R.string.dbPassword));

        Thread open = new Thread(new Runnable() {
            @Override
            public void run() {
                conn.open();
            }
        });
        open.start();

        // Setting up theme
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("theme_switch", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preference_name), Context.MODE_PRIVATE);
                Intent intent;
                if (sharedPreferences.contains("userId")){
                    intent=new Intent(SplashActivity.this,MainActivity.class);
                }
                else{
                    intent=new Intent(SplashActivity.this,LoginActivity.class);
                }
                startActivity(intent);
                finish();
            }
        },2000);
    }
}
