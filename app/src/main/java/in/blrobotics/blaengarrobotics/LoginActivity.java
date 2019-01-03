package in.blrobotics.blaengarrobotics;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONArray;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

//TODO  put dp in SharedPreferences by "dp" key

public class LoginActivity extends AppCompatActivity {
    MySQLConnection conn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /* Database Connection */
        conn = new MySQLConnection(LoginActivity.this);

        final EditText username = (EditText) findViewById(R.id.username);
        final EditText password = (EditText) findViewById(R.id.password);

        Button signInButton = (Button) findViewById(R.id.button_signIn);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValid(username) && isValid(password)){
                    String user = username.getText().toString();
                    String query = "SELECT * FROM `Users` WHERE `username`='" + user + "'";
                    AsyncTask asyncTask = conn.execute(query);
                    conn.setOnResult(new MySQLConnection.OnResult(asyncTask) {
                        @Override
                        public void getResult(Object dataObject) throws Exception{
                            JSONArray result = (JSONArray)dataObject;
                            /* Checking user exist or not */
                            if (!result.isNull(0)){
                                /* SharedPreferences */
                                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preference_name), Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                /* Clearing SharedPreferences */
                                editor.clear();
                                editor.commit();
                                String pass = MD5(password.getText().toString());
                                if (result.getJSONObject(0).getString("password").equals(pass)) {
                                    int userId = (int) result.getJSONObject(0).remove("id");
                                    /* Closing connection */
                                    //conn.close();
                                    /* Put value in to SharedPreferences */
                                    editor.putInt("userId",userId);
                                    Iterator<String> keys = result.getJSONObject(0).keys();
                                    while (keys.hasNext()){
                                        String key = keys.next();
                                        editor.putString(key,result.getJSONObject(0).getString(key));
                                    }
                                    editor.commit();
                                    /* Change activity */
                                    Intent mainActivity = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(mainActivity);
                                    finish();
                                } else {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            password.setError(getString(R.string.error_incorrect_password));
                                        }
                                    });
                                }
                            }
                            else{
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        username.setError(getString(R.string.error_invalid_user));
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
        TextView register = (TextView) findViewById(R.id.link_signup);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private String MD5(String value) throws NoSuchAlgorithmException {
        MessageDigest m = MessageDigest.getInstance("MD5");
        m.update(value.getBytes(),0,value.length());
        return new BigInteger(1,m.digest()).toString(16);
    }
    private boolean isValid(EditText editText){
        // Reset errors.
        editText.setError(null);
        // Get value
        String value = editText.getText().toString();
        // Check
        if (editText.getInputType() == 33 && value.isEmpty()){
            /* username */
            editText.setError(getString(R.string.error_field_required));
            return false;
        }
        if (editText.getInputType() == 129 && value.length()<8){
            /* password */
            editText.setError(getString(R.string.error_short_password));
            return false;
        }
        return true;
    }

    public String byteArrayToString(byte[] data){
        return new String(data);
    }
}
