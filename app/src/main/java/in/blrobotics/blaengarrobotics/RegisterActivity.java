package in.blrobotics.blaengarrobotics;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {

    MySQLConnection conn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Show the Up button in the action bar.
        ActionBar actionBar =getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        /* Database connection */
        conn = new MySQLConnection(this);

        final ViewGroup register_form = (ViewGroup) findViewById(R.id.register_form);

        /* for testing */
        /**String[] value = {"bibhasmondal96", "Bibhas", "Mondal", "xxxxxxxxxx", "xxxxxxxx@gmail.com", "xxxxxxxx","xxxxxxxx"};
        int i = 0;
        ArrayList<EditText> editTexts = getEditTextList(register_form);
        for (EditText editText:editTexts){
            String name = getResources().getResourceEntryName(editText.getId());
            editText.setText(value[i]);
            ++i;
        }*/

        Button register_button = findViewById(R.id.register_button);
        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValid(register_form)) {
                    ArrayList[] formData = getData(register_form);
                    insertIntoDatabase("Users", formData);
                }
            }
        });

        TextView login = (TextView) findViewById(R.id.link_login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
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

    public ArrayList[] getData(ViewGroup form) {
        ArrayList<String> arrayListField = new ArrayList<>();
        ArrayList<String> arrayListValue = new ArrayList<>();
        arrayListField.add("`id`");
        arrayListValue.add("NULL");
        boolean password = false;
        for (int i = 0; i < form.getChildCount(); i++) {
            View field = form.getChildAt(i);
            if (field.getClass().getSimpleName().equalsIgnoreCase("TextInputLayout")) {
                ViewGroup frameLayout = (ViewGroup) ((ViewGroup) field).getChildAt(0);
                EditText editText = (EditText) frameLayout.getChildAt(0);
                String key = getResources().getResourceEntryName(editText.getId());
                String value = editText.getText().toString();
                switch (editText.getInputType()){
                    case 129:
                        if(!password){
                            password = true;
                            arrayListField.add("`" + key + "`");
                            arrayListValue.add("MD5('" + value + "')");
                        }
                        break;
                    default:
                        arrayListField.add("`" + key + "`");
                        if (value.isEmpty()) {
                            arrayListValue.add("NULL");
                        } else {
                            arrayListValue.add("'" + value + "'");
                        }
                        break;
                }
            }
        }
        ArrayList[] data = new ArrayList[]{arrayListField, arrayListValue};
        return data;
    }

    public void insertIntoDatabase(String tableName, ArrayList[] data) {
        //INSERT INTO `Users` (`id`, `username`, `password`, `phone_no`, `email`) VALUES (NULL, 'laptop', MD5('a1b2c3d4'), '0000000000', NULL);
        String query = "INSERT INTO `" + tableName + "` (";
        query += TextUtils.join(",", data[0]);   //(`id`, `username`, `password`, `phone_no`, `email`)
        query += ") VALUES (";
        query += TextUtils.join(",", data[1]);   //(NULL, 'laptop', MD5('a1b2c3d4'), '0000000000', NULL)
        query += ")";
        AsyncTask asyncTask = conn.execute(query);
        conn.setOnResult(new MySQLConnection.OnResult(asyncTask) {
            @Override
            public void getResult(Object dataObject) throws Exception {
                if ( dataObject != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(RegisterActivity.this, getString(R.string.success_register), Toast.LENGTH_SHORT);
                            toast.show();
                            Intent login = new Intent(RegisterActivity.this,LoginActivity.class);
                            startActivity(login);
                            finish();
                        }
                    });
                }
            }
        });
    }

    public boolean isValid(ViewGroup form) {
        String password = null;
        for (int i = 0; i < form.getChildCount(); i++) {
            View field = form.getChildAt(i);
            if (field.getClass().getSimpleName().equalsIgnoreCase("TextInputLayout")) {
                ViewGroup frameLayout = (ViewGroup) ((ViewGroup) field).getChildAt(0);
                EditText editText = (EditText) frameLayout.getChildAt(0);
                // Reset errors.
                editText.setError(null);
                // Get value
                String value = editText.getText().toString();
                switch (editText.getInputType()) {
                    case 1:
                        //simple text
                        if (value.isEmpty()) {
                            editText.setError(getString(R.string.error_field_required));
                            return false;
                        }
                        break;
                    case 33:
                        //email
                        if (!Patterns.EMAIL_ADDRESS.matcher(value).matches() && value.length()>0) {
                            editText.setError(getString(R.string.error_invalid_email));
                            return false;
                        }

                    case 97:
                        //textPersonName
                        break;

                    case 129:
                        //password
                        if (value.length() < 8) {
                            editText.setError(getString(R.string.error_short_password));
                            return false;
                        }
                        if (password == null) {
                            password = value;
                        } else if (!value.equals(password)) {
                            //password dont match
                            editText.setError(getString(R.string.error_invalid_password));
                            return false;
                        }
                        break;

                    case 193:
                        //phone
                        if (!Patterns.PHONE.matcher(value).matches() || value.length()<10) {
                            editText.setError(getString(R.string.error_invalid_phone));
                            return false;
                        }
                        break;
                }
            }
        }
        return true;
    }


    public ArrayList<EditText> getEditTextList(ViewGroup form) {
        ArrayList<EditText> editTextArrayList = new ArrayList<>();
        for (int i = 0; i < form.getChildCount(); i++) {
            View field = form.getChildAt(i);
            if (field.getClass().getSimpleName().equalsIgnoreCase("TextInputLayout")) {
                ViewGroup frameLayout = (ViewGroup) ((ViewGroup) field).getChildAt(0);
                EditText editText = (EditText) frameLayout.getChildAt(0);
                editTextArrayList.add(editText);
            }
        }
        return editTextArrayList;
    }
}
