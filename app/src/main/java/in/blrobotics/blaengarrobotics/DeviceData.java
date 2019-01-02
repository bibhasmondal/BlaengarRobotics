package in.blrobotics.blaengarrobotics;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.jjoe64.graphview.series.DataPoint;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class DeviceData extends AppCompatActivity {
    MySQLConnection conn;
    private int deviceId;
    private int lastReceivedId = 0;
    private final Handler handler = new Handler();
    private Runnable runnable;
    private int syncTime = 5000;

    protected GraphFragment speed;
    protected GraphFragment acceleration;
    protected GraphFragment angle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_data);

        // Show the Up button in the action bar.
        ActionBar actionBar =getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        /* database connection */
        conn = new MySQLConnection(this);

        Intent intent = getIntent();
        deviceId = intent.getIntExtra("deviceId",0); // 0 because sql id starts from 1

        TextView textView = (TextView) findViewById(R.id.tv_device);
        textView.setText("Device Serial No: "+getSNFromId(deviceId));

        /* Getting data from database */
        String query = "SELECT * FROM Data WHERE `device` = "+deviceId;
        JSONArray result = (JSONArray) conn.execute(query);

        List<LatLng> points = new ArrayList();
        for(int i=0;i<result.length();++i){
            try {
                JSONObject items = result.getJSONObject(i);
                LatLng position = new LatLng(items.getDouble("lat"),(items.getDouble("lon")));
                points.add(position);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        /**Loading map*/
        // Getting reference to the SupportMapFragment of device_data.xml
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // Getting GoogleMap object from the fragment
        MapCallback map = new MapCallback(this,points);
        supportMapFragment.getMapAsync(map);

        /** Setting data to the fragment*/
        // getting speed fragment
        speed = (GraphFragment) getSupportFragmentManager().findFragmentById(R.id.speed);
        speed.setPlotTitle("Speed");
        speed.setAxisTitle("","Speed");
        speed.addLines(1);
        speed.setLinesColor(Arrays.asList("red"));
        // getting acceleration fragment
        acceleration = (GraphFragment) getSupportFragmentManager().findFragmentById(R.id.acceleration);
        acceleration.setPlotTitle("Acceleration");
        acceleration.setAxisTitle("","Acceleration");
        acceleration.addLines(3);
        acceleration.setLinesColor(Arrays.asList("red","green","blue"));
        acceleration.setLinesTitle(Arrays.asList("Acc_X","Acc_Y","Acc_Z"));
        // getting angle fragment
        angle = (GraphFragment) getSupportFragmentManager().findFragmentById(R.id.angle);
        angle.setPlotTitle("Angle");
        angle.setAxisTitle("","Angle");
        angle.addLines(3);
        angle.setLinesColor(Arrays.asList("red","green","blue"));
        angle.setLinesTitle(Arrays.asList("Ang_X","Ang_Y","Ang_Z"));

        //Getting Data for using deviceID
        List[] data = getData(deviceId);
        List<DataPoint[]> speedList = data[0];
        List<DataPoint[]> accList = data[1];
        List<DataPoint[]> angList = data[2];
        // setting value to the fragment
        speed.setData(speedList);
        acceleration.setData(accList);
        angle.setData(angList);
    }

    @Override
    public void onResume() {
        super.onResume();
        runnable = new Runnable() {
            @Override
            public void run() {
                // TODO //synchronization time have to be set from setting
                //Getting Data for using deviceID
                List[] data = getData(deviceId);
                List<DataPoint[]> speedList = data[0];
                List<DataPoint[]> accList = data[1];
                List<DataPoint[]> angList = data[2];
                //appending data
                speed.appendData(speedList);
                acceleration.appendData(accList);
                angle.appendData(angList);
                // Works like infinite loop
                handler.postDelayed(this, syncTime);
            }
        };
        handler.postDelayed(runnable, syncTime);
    }

    @Override
    public void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
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

    private String getSNFromId(int id){
        String SN = "";
        JSONArray result = (JSONArray) conn.execute("SELECT `serial_no` FROM `Devices` WHERE `id` = "+id);
        try {
            SN = result.getJSONObject(0).getString("serial_no");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return SN;
    }

    public List[] getData(int deviceId){
        String query = "SELECT * FROM `Data` WHERE `device` = "+deviceId+" AND `id` > "+lastReceivedId+" ORDER BY `id` DESC LIMIT 100";
        JSONArray result = (JSONArray) conn.execute(query);
        int length = result.length();
        DataPoint[] speed = new DataPoint[length];
        DataPoint[] acc_x = new DataPoint[length];
        DataPoint[] acc_y = new DataPoint[length];
        DataPoint[] acc_z = new DataPoint[length];
        DataPoint[] ang_x = new DataPoint[length];
        DataPoint[] ang_y = new DataPoint[length];
        DataPoint[] ang_z = new DataPoint[length];
        int index = 0;
        for (int i=result.length()-1;i>=0;i--) {
            try {
                JSONObject items = result.getJSONObject(i);
                lastReceivedId = items.getInt("id");
                speed[index] = new DataPoint((double) index, Double.parseDouble(items.getString("speed").replace(" Kph","")));
                acc_x[index] = new DataPoint((double) index, Double.parseDouble(items.getString("acc_x").replace(" m/s","")));
                acc_y[index] = new DataPoint((double) index, Double.parseDouble(items.getString("acc_y").replace(" m/s","")));
                acc_z[index] = new DataPoint((double) index, Double.parseDouble(items.getString("acc_z").replace(" m/s","")));
                ang_x[index] = new DataPoint((double) index, Double.parseDouble(items.getString("ang_x").replace(" deg","")));
                ang_y[index] = new DataPoint((double) index, Double.parseDouble(items.getString("ang_y").replace(" deg","")));
                ang_z[index] = new DataPoint((double) index, Double.parseDouble(items.getString("ang_z").replace(" deg","")));
                ++index;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        List<DataPoint[]> speedList = Arrays.asList(speed,null);
        List<DataPoint[]> accList = Arrays.asList(acc_x,acc_y,acc_z);
        List<DataPoint[]> angList = Arrays.asList(ang_x,ang_y,ang_z);
        return new List[]{speedList,accList,angList};
    }
}
