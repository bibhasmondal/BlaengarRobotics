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
        conn.open();

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
//                    int userId = (int) sharedPreferences.getInt("userId",0); //0 because it start from 1 in mysql
////                    if (sharedPreferences.getString("password","").equals(getPasswordUserById(userId))){
////                        intent=new Intent(SplashActivity.this,MainActivity.class);
////                    }
////                    else{
////                        intent=new Intent(SplashActivity.this,LoginActivity.class);
////                    }
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

//    private String getPasswordUserById(int userId){
//        try{
//            String query = "SELECT `password` FROM `Users` WHERE `id`=" + userId;
//            conn.execute(query);
//            JSONArray result = (JSONArray)conn.execute(query);
//            if (result != null){
//                return result.getJSONObject(0).getString("password");
//            }
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
//        return null;
//    }
}
