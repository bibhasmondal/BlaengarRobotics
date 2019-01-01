package in.blrobotics.blaengarrobotics;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class DeviceData extends AppCompatActivity {
    MySQLConnection conn;
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
        int deviceId = intent.getIntExtra("deviceId",0); // 0 because sql id starts from 1

        TextView textView = (TextView) findViewById(R.id.tv_device);
        textView.setText("Device Id: "+String.valueOf(deviceId));

        /* Getting data from database */
        String query = "SELECT * FROM Data WHERE `device`="+deviceId;
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

        /**Load map*/
        // Getting reference to the SupportMapFragment of device_data.xml
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // Getting GoogleMap object from the fragment
        MapCallback map = new MapCallback(this,points);
        supportMapFragment.getMapAsync(map);
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
}
