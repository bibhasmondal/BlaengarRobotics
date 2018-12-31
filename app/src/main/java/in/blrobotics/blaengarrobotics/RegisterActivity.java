package in.blrobotics.blaengarrobotics;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
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
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends AppCompatActivity {
    private UserLoginTask mAuthTask = null;
    // UI references.
    private EditText firstNameText;
    private EditText lastNameText;
    private EditText emailText;
    private EditText mobileText;
    private EditText passwordText;
    private EditText reEnterPasswordText;
    private View mProgressView;
    private View mSignupFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Set up the login form.
        firstNameText=(EditText) findViewById(R.id.input_firstname);
        lastNameText=(EditText) findViewById(R.id.input_lastname);
        emailText=(EditText) findViewById(R.id.input_email);
        mobileText=(EditText) findViewById(R.id.input_mobile);
        passwordText=(EditText) findViewById(R.id.input_password);
        reEnterPasswordText=(EditText) findViewById(R.id.input_reEnterPassword);

        Button createAccountButton = (Button) findViewById(R.id.register_button);
        createAccountButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mSignupFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.register_progress);
    }
    private void attemptRegister() {
        if (mAuthTask != null) {
            return;
        }
        // Reset errors.
        firstNameText.setError(null);
        lastNameText.setError(null);
        emailText.setError(null);
        mobileText.setError(null);
        passwordText.setError(null);
        reEnterPasswordText.setError(null);

        // Store values at the time of the login attempt.
        String firstname = firstNameText.getText().toString();
        String lastname = lastNameText.getText().toString();
        String email = emailText.getText().toString();
        String mobile_no = mobileText.getText().toString();
        String password = passwordText.getText().toString();
        String confirmPassword = reEnterPasswordText.getText().toString();
        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) && !isPasswordValid(password,confirmPassword)) {
            passwordText.setError(getString(R.string.error_invalid_password));
            focusView = reEnterPasswordText;
            cancel = true;
        }

        // Check for a valid email address.
        if (!TextUtils.isEmpty(email)&&!isEmailValid(email)) {
            emailText.setError(getString(R.string.error_invalid_email));
            focusView =emailText;
            cancel = true;
        }

        //Check for a valid phone no
        if(TextUtils.isEmpty(mobile_no)){
            mobileText.setError("This field is required");
            focusView = mobileText;
            cancel=true;
        }
        else if(!isPhoneValid(mobile_no)){
            mobileText.setError(getString(R.string.error_invalid_mobile));
            focusView = mobileText;
            cancel=true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(firstname,lastname,email,mobile_no,password);
            mAuthTask.execute("http://api.blrobotics.in/BRIOT0010/signup.php");
        }
    }

    private boolean isPhoneValid(String phone_no) {
        //TODO: Replace this with your own logic
        return phone_no.length()>=10;
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password,String confirmPassword) {
        //TODO: Replace this with your own logic

        return password==confirmPassword;
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

            mSignupFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mSignupFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSignupFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mSignupFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<String, Void, Boolean> {
        private final String firstName;
        private final String lastName;
        private final String email;
        private final String phoneNo;
        private final String password;

        UserLoginTask(String firstname,String lastname,String email, String mobile, String password) {
            this.firstName=firstname;
            this.lastName=lastname;
            this.email = email;
            this.phoneNo=mobile;
            this.password = password;
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
                post_data= URLEncoder.encode("firstName","UTF-8")+"="+URLEncoder.encode(firstName,"UTF-8")+"&"+
                        URLEncoder.encode("lastName","UTF-8")+"="+URLEncoder.encode(lastName,"UTF-8")+"&"+
                        URLEncoder.encode("email","UTF-8")+"="+URLEncoder.encode(email,"UTF-8")+"&"+
                        URLEncoder.encode("phoneNo","UTF-8")+"="+URLEncoder.encode(phoneNo,"UTF-8")+"&"+
                        URLEncoder.encode("password","UTF-8")+"="+URLEncoder.encode(password,"UTF-8");
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
                Log.i("Result::::::::::::::::",result);
                return true;
            }
            else{
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Log.i("success","200");
                Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}

