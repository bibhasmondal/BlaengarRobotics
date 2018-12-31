package in.blrobotics.blaengarrobotics;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.json.JSONArray;
import java.util.*;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    MySQLConnection conn;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* connecting to database */
        conn = new MySQLConnection(this);
        /* getSharedPreferences */
        sharedPreferences = getSharedPreferences(getString(R.string.shared_preference_name), Context.MODE_PRIVATE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,AddDevice.class);
                startActivity(intent);
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /* Set Nav header name and email */
        View hView =  navigationView.getHeaderView(0);
        ImageView dp = hView.findViewById(R.id.imageDP);
        TextView name = hView.findViewById(R.id.name);
        TextView email = hView.findViewById(R.id.email);

        Bitmap image = byteArrayToBitmap(stringToByteArray(sharedPreferences.getString("dp",null)));
        if (image!=null){
            dp.setImageBitmap(image);
        }

        name.setText(sharedPreferences.getString("f_name","")+" "+sharedPreferences.getString("l_name",""));
        email.setText(sharedPreferences.getString("email",""));


        /* List view */
        listRefresh();

        /* Refresh layout */
        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                listRefresh();
                //when your data has finished loading, set the refresh state of the view to false
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()){
            case R.id.action_about:
//                Intent about = new Intent(this,AboutActivity.class);
//                startActivity(about);
                return  true;
            case R.id.action_settings:
//                Intent settings = new Intent(this,SettingsActivity.class);
//                startActivity(settings);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()){
            case R.id.nav_dashboard:
                break;

            case R.id.nav_add:
                Intent add = new Intent(this,AddDevice.class);
                startActivity(add);
                break;

            case R.id.nav_account:
//                Intent account = new Intent(this,AccountActivity.class);
//                startActivity(account);
                break;

            case R.id.nav_signout:
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.commit();
                Intent login = new Intent(this,LoginActivity.class);
                startActivity(login);
                finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void listRefresh(){
        /* User dashboard */
        if (sharedPreferences.contains("userId")) {
            int userId = sharedPreferences.getInt("userId", 0);
            List[] data = getDataFromUserId(userId);
            if (data != null){
                List<List> dataset = data[0];
                List<Integer> positionToId = data[1];
                /* Custom array adapter */
                ListView listView = (ListView) findViewById(R.id.idListView);
                CustomAdapter customAdapter = new CustomAdapter(this, R.layout.dashboard, dataset,positionToId);
                listView.setAdapter(customAdapter);
            }

//                    conn.close();
        }
        else{
            /* Custom array adapter */
            ListView listView = (ListView) findViewById(R.id.idListView);
            listView.setAdapter(null);
        }
    }

    public List[] getDataFromUserId(int userId){
        try {
            String query = "SELECT * FROM `Devices` WHERE `id` IN (SELECT `device` FROM `Owners` WHERE `user`=" + userId + ")";
            JSONArray deviceDetails = (JSONArray) conn.execute(query);
            if (deviceDetails != null ) {
                List<List> dataset = new ArrayList();
                List<Integer> mapPositionToId = new ArrayList();
                for (int i = 0; i < deviceDetails.length(); i++) {

                    int deviceID = (int)deviceDetails.getJSONObject(i).remove("id");
                    /* Mapping index for array to its corresponding ID */
                    mapPositionToId.add(i,deviceID);

                    List<String> data = new ArrayList();
                    for (Iterator<String> itDetails = deviceDetails.getJSONObject(i).keys(); itDetails.hasNext(); ) {
                        String keyDetails = itDetails.next();
                        data.add(deviceDetails.getJSONObject(i).getString(keyDetails));
                    }
                    query = "SELECT * FROM `Data` WHERE `device` =" + deviceID + " ORDER BY `id` DESC LIMIT 1";
                    JSONArray deviceData = (JSONArray) conn.execute(query);
                    if (!deviceData.isNull(0)) {
                        for (int j = 0; j < deviceData.length(); j++) {
                            deviceData.getJSONObject(j).remove("id");
                            deviceData.getJSONObject(j).remove("device");
                            for (Iterator<String> itData = deviceData.getJSONObject(j).keys(); itData.hasNext(); ) {
                                String keyData = itData.next();
                                data.add(deviceData.getJSONObject(j).getString(keyData));
                            }
                        }
                    }
                    else{
                        String[] temp = new String[10];
                        Arrays.fill(temp,"No data available");
                        data.addAll(Arrays.asList(temp));
                    }
                    dataset.add(data);
                }
                return new List[]{dataset, mapPositionToId};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] stringToByteArray (String data){
        if (data != null){
            return data.getBytes();
        }
        return null;
    }

    public Bitmap byteArrayToBitmap(byte[] byteArray){
        if (byteArray != null){
            if (byteArray.length>0){
                return BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);
            }
        }
        return  null;
    }
}
