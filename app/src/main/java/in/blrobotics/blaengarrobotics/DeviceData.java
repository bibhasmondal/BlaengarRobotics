package in.blrobotics.blaengarrobotics;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.jjoe64.graphview.series.DataPoint;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class DeviceData extends AppCompatActivity {
    MySQLConnection conn;
    private int deviceId;
    private int lastReceivedId = 0;
    private final Handler handler = new Handler();
    private Runnable runnable;
    private int syncTime = 5000;

    protected MySupportMapFragment mySupportMapFragment;
    protected MapCallback map;
    protected GraphFragment speed;
    protected GraphFragment acceleration;
    protected GraphFragment angle;

    TextView textView;

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

        textView = (TextView) findViewById(R.id.tv_device);
        // getting serial no from device id
        getSNFromId(deviceId);



        /**Loading map*/
        // Getting reference to the SupportMapFragment of device_data.xml
        mySupportMapFragment = (MySupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // Adding feature for nested scrolling
        final ScrollView mScrollView = (ScrollView) findViewById(R.id.sv_container);
        mySupportMapFragment.setListener(new MySupportMapFragment.OnTouchListener() {
            @Override
            public void onTouch() {
                mScrollView.requestDisallowInterceptTouchEvent(true);
            }
        });


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


        // setting value to the fragment
        getData(deviceId,false);
    }

    @Override
    public void onResume() {
        super.onResume();
        runnable = new Runnable() {
            @Override
            public void run() {
                // TODO //synchronization time have to be set from setting
                getData(deviceId,true);
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

    private void getSNFromId(int id){
        AsyncTask asyncTask = conn.execute("SELECT `serial_no` FROM `Devices` WHERE `id` = "+id);
        conn.setOnResult(new MySQLConnection.OnResult(asyncTask) {
            @Override
            public void getResult(Object dataObject) throws Exception {
                JSONArray result = (JSONArray)dataObject;
                final String sn = result.getJSONObject(0).getString("serial_no");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("Device Serial No: "+sn);
                    }
                });
            }
        });
    }

    public void getData(int deviceId, final boolean async){
        final List<LatLng> points = new ArrayList();
        final List<DataPoint[]> speedList = new ArrayList();
        final List<DataPoint[]> accList = new ArrayList();
        final List<DataPoint[]> angList = new ArrayList();
        String query = "SELECT * FROM `Data` WHERE `device` = "+deviceId+" AND `id` > "+lastReceivedId+" ORDER BY `id` DESC LIMIT 100";
        AsyncTask asyncTask = conn.execute(query);
        conn.setOnResult(new MySQLConnection.OnResult(asyncTask) {
            @Override
            public void getResult(Object dataObject) throws Exception {
                JSONArray result = (JSONArray)dataObject;
                int index = 0;
                int length = result.length();
                DataPoint[] spd = new DataPoint[length];
                DataPoint[] acc_x = new DataPoint[length];
                DataPoint[] acc_y = new DataPoint[length];
                DataPoint[] acc_z = new DataPoint[length];
                DataPoint[] ang_x = new DataPoint[length];
                DataPoint[] ang_y = new DataPoint[length];
                DataPoint[] ang_z = new DataPoint[length];
                for (int i=result.length()-1;i>=0;i--) {
                    JSONObject items = result.getJSONObject(i);
                    lastReceivedId = items.getInt("id");
                    points.add(new LatLng(items.getDouble("lat"),(items.getDouble("lon"))));
                    spd[index] = new DataPoint((double) index, Double.parseDouble(items.getString("speed").replace(" Kph","")));
                    acc_x[index] = new DataPoint((double) index, Double.parseDouble(items.getString("acc_x").replace(" m/s","")));
                    acc_y[index] = new DataPoint((double) index, Double.parseDouble(items.getString("acc_y").replace(" m/s","")));
                    acc_z[index] = new DataPoint((double) index, Double.parseDouble(items.getString("acc_z").replace(" m/s","")));
                    ang_x[index] = new DataPoint((double) index, Double.parseDouble(items.getString("ang_x").replace(" deg","")));
                    ang_y[index] = new DataPoint((double) index, Double.parseDouble(items.getString("ang_y").replace(" deg","")));
                    ang_z[index] = new DataPoint((double) index, Double.parseDouble(items.getString("ang_z").replace(" deg","")));
                    ++index;
                }
                speedList.addAll(Arrays.asList(spd,null));
                accList.addAll(Arrays.asList(acc_x,acc_y,acc_z));
                angList.addAll(Arrays.asList(ang_x,ang_y,ang_z));
                while (true){
                    if (index == length){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (async){
                                    //appending data
                                    if (map != null)map.appendData(points);
                                    speed.appendData(speedList);
                                    acceleration.appendData(accList);
                                    angle.appendData(angList);
                                }
                                else{

                                    System.out.println(angList);
                                    // Getting GoogleMap object from the fragment
                                    map = new MapCallback(DeviceData.this,points);
                                    mySupportMapFragment.getMapAsync(map);
                                    // setting value to the fragment
                                    speed.setData(speedList);
                                    acceleration.setData(accList);
                                    angle.setData(angList);
                                }
                            }
                        });
                        return;
                    }
                }
            }

        });
    }
}
