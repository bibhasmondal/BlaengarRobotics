package in.blrobotics.blaengarrobotics;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class AccountActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        // Show the Up button in the action bar.
        ActionBar actionBar =getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        /* getSharedPreferences */
        sharedPreferences = getSharedPreferences(getString(R.string.shared_preference_name), Context.MODE_PRIVATE);

        TextView name = findViewById(R.id.user_profile_name);
        TextView email = findViewById(R.id.user_profile_email);
        TextView username = findViewById(R.id.user_profile_username);
        TextView phone = findViewById(R.id.user_profile_phone);

        name.setText(sharedPreferences.getString("f_name","")+" "+sharedPreferences.getString("l_name",""));
        email.setText(sharedPreferences.getString("email",""));
        username.setText(sharedPreferences.getString("username",""));
        phone.setText(sharedPreferences.getString("phone_no",""));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
