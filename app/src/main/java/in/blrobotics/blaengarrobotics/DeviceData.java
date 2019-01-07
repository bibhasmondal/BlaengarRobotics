package in.blrobotics.blaengarrobotics;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import com.google.android.gms.maps.model.LatLng;
import com.jjoe64.graphview.series.DataPoint;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeviceData extends AppCompatActivity implements ActionBar.TabListener {
    MySQLConnection conn;
    private int deviceId;
    private int lastReceivedId = 0;
    private double lastX = 0;
    private final Handler handler = new Handler();
    private Runnable runnable;
    private int syncTime = 5000;

    protected MySupportMapFragment mySupportMapFragment;
    protected ScrollMapCallback map;
    protected GraphFragment speed;
    protected GraphFragment temp;
    protected GraphFragment acceleration;
    protected GraphFragment angle;

    protected ViewPager pager;
    protected Bundle bundle;
    protected ActionBar actionBar;
    TextView textView;
    boolean isTabView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_data);

        /* database connection */
        conn = new MySQLConnection(this);

        // Show the Up button in the action bar.
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


        Intent intent = getIntent();
        deviceId = intent.getIntExtra("deviceId",0); // 0 because sql id starts from 1

        textView = (TextView) findViewById(R.id.tv_device);
        // getting serial no from device id
        getSNFromId(deviceId);

        // Setting up view type
        if (PreferenceManager.getDefaultSharedPreferences(this).getString("view_type", "1").equals("1")) {
            isTabView = true;
        } else {
            isTabView = false;
        }

        if(isTabView){
            tabView();
        }
        else {
            scrollView();
        }
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
        if (!isTabView)handler.postDelayed(runnable, syncTime);
    }

    @Override
    public void onPause() {
        if (!isTabView)handler.removeCallbacks(runnable);
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

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        // When the tab is selected, switch to the
        // corresponding page in the ViewPager.
        pager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

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


    public void tabView(){
        /** Setting pager visible*/
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setVisibility(View.VISIBLE);
        //Map
        bundle = new Bundle();
        bundle.putInt("deviceId",1);
        PagerMapFragment map = new PagerMapFragment();
        map.setArguments(bundle);

        //speed and temp
        bundle = new Bundle();
        bundle.putInt("deviceId",1);
        bundle.putStringArray("Speed",new String[]{"speed"});
        bundle.putStringArray("Temp",new String[]{"temp"});
        PagerGraphFragment spdtmp = new PagerGraphFragment();
        spdtmp.setArguments(bundle);

        // Acceleration and angle
        bundle = new Bundle();
        bundle.putInt("deviceId",1);
        bundle.putStringArray("Acceleration",new String[]{"acc_x","acc_y","acc_z"});
        bundle.putStringArray("Angle",new String[]{"ang_x","ang_y","ang_z"});
        PagerGraphFragment accang = new PagerGraphFragment();
        accang.setArguments(bundle);


        // Adding fragment to the pager
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(map);
        viewPagerAdapter.addFragment(accang);
        viewPagerAdapter.addFragment(spdtmp);
        pager.setAdapter(viewPagerAdapter);


        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.addTab(actionBar.newTab().setText("Location").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText("Acc & Angle").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText("Speed & Temp").setTabListener(this));

        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between pages, select the
                // corresponding tab.
                getSupportActionBar().setSelectedNavigationItem(position);
            }
        });

    }

    public void scrollView(){
        /**Setting scrollview visible*/
        final ScrollView mScrollView = (ScrollView) findViewById(R.id.sv_container);
        mScrollView.setVisibility(View.VISIBLE);
        // Loading map
        // Getting reference to the SupportMapFragment of device_data.xml
        mySupportMapFragment = (MySupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // Adding feature for nested scrolling
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
        // getting temperature fragment
        temp = (GraphFragment) getSupportFragmentManager().findFragmentById(R.id.temp);
        temp.setPlotTitle("Temperature");
        temp.setAxisTitle("","Temperature");
        temp.addLines(1);
        temp.setLinesColor(Arrays.asList("red"));
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

    public void getData(int deviceId, final boolean async){
        final List<LatLng> points = new ArrayList();
        final List<DataPoint[]> speedList = new ArrayList();
        final List<DataPoint[]> tempList = new ArrayList();
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
                DataPoint[] tmp = new DataPoint[length];
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
                    spd[index] = new DataPoint(lastX, Double.parseDouble(items.getString("speed").replaceAll("[^0-9.]", "")));
                    tmp[index] = new DataPoint(lastX, Double.parseDouble(items.getString("temp").replaceAll("[^0-9.]", "")));
                    acc_x[index] = new DataPoint(lastX, Double.parseDouble(items.getString("acc_x").replaceAll("[^0-9.]", "")));
                    acc_y[index] = new DataPoint(lastX, Double.parseDouble(items.getString("acc_y").replaceAll("[^0-9.]", "")));
                    acc_z[index] = new DataPoint(lastX, Double.parseDouble(items.getString("acc_z").replaceAll("[^0-9.]", "")));
                    ang_x[index] = new DataPoint(lastX, Double.parseDouble(items.getString("ang_x").replaceAll("[^0-9.]", "")));
                    ang_y[index] = new DataPoint(lastX, Double.parseDouble(items.getString("ang_y").replaceAll("[^0-9.]", "")));
                    ang_z[index] = new DataPoint(lastX, Double.parseDouble(items.getString("ang_z").replaceAll("[^0-9.]", "")));
                    ++index;++lastX;
                }
                speedList.addAll(Arrays.asList(spd,null));
                tempList.addAll((Arrays.asList(tmp,null)));
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
                                    temp.appendData(tempList);
                                    acceleration.appendData(accList);
                                    angle.appendData(angList);
                                }
                                else{
                                    // Getting GoogleMap object from the fragment
                                    map = new ScrollMapCallback(DeviceData.this,points);
                                    mySupportMapFragment.getMapAsync(map);
                                    // setting value to the fragment
                                    speed.setData(speedList);
                                    temp.setData(tempList);
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
