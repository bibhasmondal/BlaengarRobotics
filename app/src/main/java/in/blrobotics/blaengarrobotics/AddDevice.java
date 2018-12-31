package in.blrobotics.blaengarrobotics;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AddDevice extends AppCompatActivity {
    MySQLConnection conn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_device);
        /* database connection */
        conn = new MySQLConnection(this);

        //Getting the instance of AutoCompleteTextView
        final AutoCompleteTextView deviceSlNo =  (AutoCompleteTextView)findViewById(R.id.device_sl_no);

        // Getting the instance of Button
        Button device_submit = findViewById(R.id.device_submit);

        try {
            JSONArray result = (JSONArray)conn.execute("SELECT `serial_no` FROM `Devices`");
            List<String> deviceList = new ArrayList<String>();

            for (int i = 0; i < result.length(); i++) {
                final JSONObject e = result.getJSONObject(i);
                String name = e.getString("serial_no");
                deviceList.add(name);
            }

            /* Creating the instance of ArrayAdapter containing list of device serial no */
            ArrayAdapter<String> deviceListAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, deviceList);

            deviceSlNo.setThreshold(1);//will start working from first character
            deviceSlNo.setAdapter(deviceListAdapter);//setting the adapter data into the AutoCompleteTextView
            deviceSlNo.setTextColor(Color.BLACK);


        } catch (Exception e) {
            e.printStackTrace();
        }


        device_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                String name = getResources().getResourceEntryName(view.getId());
//                Toast toast = Toast.makeText(AddDevice.this,name,Toast.LENGTH_SHORT);
//                toast.show();
                Integer serialNo = Integer.parseInt(deviceSlNo.getText().toString());
                try {
                    JSONArray result = (JSONArray)conn.execute("SELECT `id` from `Devices` where serial_no='"+serialNo+"'");
                    SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preference_name), Context.MODE_PRIVATE);

                    if (!result.isNull(0) && sharedPreferences.contains("userId")){
                        int userId = sharedPreferences.getInt("userId",0);
                        int deviceId = result.getJSONObject(0).getInt("id");
                        String query = "INSERT INTO `Owners` (`id`, `device`, `user`) VALUES (NULL, "+deviceId+", "+userId+")";
                        if ((Integer) conn.execute(query) != null){
                            /* Closing connection */
                            //conn.close();
                            /* Make toast */
                            Toast toast = Toast.makeText(AddDevice.this,getString(R.string.added_device),Toast.LENGTH_LONG);
                            toast.show();

                            /* Get intent and go back */
                            Intent mainActivity = new Intent(AddDevice.this,MainActivity.class);
                            startActivity(mainActivity);
                        }
                        else{
                            /* Make toast */
                            Toast toast = Toast.makeText(AddDevice.this,getString(R.string.error_already_added),Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                    else{
                        Toast toast = Toast.makeText(AddDevice.this,getString(R.string.error_device),Toast.LENGTH_LONG);
                        toast.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
