package in.blrobotics.blaengarrobotics;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity{
    private UserLoginTask mAuthTask = null;

    // UI references.
    String fileName="Login";
    private AutoCompleteTextView mEmailPhoneView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    SharedPreferences sharedPreferences = null;
    SharedPreferences.Editor editor=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailPhoneView = (AutoCompleteTextView) findViewById(R.id.email_ph);

        mPasswordView = (EditText) findViewById(R.id.password);

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        TextView register=(TextView) findViewById(R.id.link_signup);
        register.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        sharedPreferences = getSharedPreferences(fileName, Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();


    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailPhoneView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String emailPhone = mEmailPhoneView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(emailPhone)) {
            mEmailPhoneView.setError(getString(R.string.error_field_required));
            focusView = mEmailPhoneView;
            cancel = true;
        } /*else if (!isEmail_PhoneValid(emailPhone)) {
            mEmailPhoneView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailPhoneView;
            cancel = true;
        }*/

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(emailPhone, password);
            mAuthTask.execute("http://api.blrobotics.in/BRIOT0010/login.php");
        }
    }

    private boolean isEmail_PhoneValid(String ph_email) {
        //TODO: Replace this with your own logic
        return ph_email.contains("@") || ph_email.length()>10;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }

    }

    public class UserLoginTask extends AsyncTask<String, Void, Boolean> {

        private final String mUserName;
        private final String mPassword;

        UserLoginTask(String userName, String password) {
            mUserName = userName;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(String... urlString) {
            // TODO: attempt authentication against a network service.

            OutputStream outputStream = null;
            OutputStreamWriter outputStreamWriter=null;
            String post_data=null;
            URL url = null;
            try {
                url = new URL(urlString[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return false;
            }
            URLConnection connection = null;
            try {
                connection = url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            try {
                httpConnection.setRequestMethod("POST");
            } catch (ProtocolException e) {
                e.printStackTrace();
                return false;
            }
            httpConnection.setDoOutput(true);
            try {
                outputStream = httpConnection.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            try {
                outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            }
            BufferedWriter bufferedWriter=new BufferedWriter(outputStreamWriter);

            try {
                post_data= URLEncoder.encode("phone_no","UTF-8")+"="+URLEncoder.encode(mUserName,"UTF-8")+"&"+
                        URLEncoder.encode("password","UTF-8")+"="+URLEncoder.encode(mPassword,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            }
            try {
                bufferedWriter.write(post_data);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            try {
                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            try {
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            InputStream inputStream=null;
            InputStreamReader inputStreamReader=null;
            try {
                inputStream=httpConnection.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            try {
                inputStreamReader=new InputStreamReader(inputStream,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            }
            BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
            String result="";
            String line;
            try {
                while ((line=bufferedReader.readLine())!=null){
                    result+=line;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            httpConnection.disconnect();
            // TODO: register the new account here.
            if (result!="false"){
                Log.i("res::::::::::::::::::::",result);
                editor.putString("phone_no",mUserName);
                editor.putString("password",mPassword);
                editor.commit();
                return true;
            }
            else{
                editor.clear();
                editor.commit();
                return false;
            }
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Log.i("success","200");
                Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
                //finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

