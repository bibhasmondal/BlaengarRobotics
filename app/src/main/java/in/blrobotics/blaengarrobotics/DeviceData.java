package in.blrobotics.blaengarrobotics;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;
import org.json.JSONArray;

public class DeviceData extends AppCompatActivity implements ActionBar.TabListener {
    MySQLConnection conn;
    private int deviceId;
    protected ViewPager pager;
    protected Bundle bundle;
    protected ActionBar actionBar;

    TextView textView;

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

        tabView();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
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
        pager = (ViewPager) findViewById(R.id.pager);
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
}
