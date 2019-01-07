package in.blrobotics.blaengarrobotics;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PagerMapFragment extends Fragment implements OnMapReadyCallback {
    private Activity activity;
    GoogleMap googleMap;
    Marker marker;
    LatLng lastLatLng = null;
    private int lastReceivedId = 1;
    MySQLConnection conn;
    private final Handler handler = new Handler();
    private Runnable runnable;
    private int syncTime = 10000;
    private int deviceId;

    public PagerMapFragment(){
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstateState) {
        if (getArguments().containsKey("deviceId")){
            deviceId = getArguments().getInt("deviceId");
            getArguments().remove("deviceId");
        }
        super.onCreate(savedInstateState);

        // Setting up Sync time
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getContext());
        syncTime = Integer.parseInt(preference.getString("data_sync","10000"));

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pager_map, container, false);
        lastReceivedId = 1;
        // Getting reference to the SupportMapFragment of device_data.xml
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        this.activity = activity;
        /* database connection */
        conn = new MySQLConnection(activity);
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        runnable = new Runnable() {
            @Override
            public void run() {
                // TODO //synchronization time have to be set from setting
                getData(deviceId);
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
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        // setting night mode on map
        if (PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("theme_switch", false)) {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(activity, R.raw.dark_map));
        }
        getData(deviceId);

    }

    public void getData(int deviceId){
        final List<LatLng> points = new ArrayList();
        String query = "SELECT `id`,`lat`,`lon` FROM `Data` WHERE `device` = "+deviceId+" AND `id` >= "+lastReceivedId+" ORDER BY `id` DESC LIMIT 100";
        AsyncTask asyncTask = conn.execute(query);
        conn.setOnResult(new MySQLConnection.OnResult(asyncTask) {
            @Override
            public void getResult(Object dataObject) throws Exception {
                JSONArray result = (JSONArray)dataObject;
                final List<LatLng> points = new ArrayList<>();
                for (int i=result.length()-1;i>=0;i--) {
                    JSONObject items = result.getJSONObject(i);
                    lastReceivedId = items.getInt("id");
                    points.add(new LatLng(items.getDouble("lat"),items.getDouble("lon")));
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addPolyline(points);
                        setLocation(points);
                    }
                });
            }

        });
    }

    // set location to google maps
    public void setLocation(List<LatLng> points) {
        if (points != null && !points.isEmpty()){
            lastLatLng = points.get(points.size()-1);
        }
        // Add marker to the map
        if (marker != null){
            marker.remove();
        }
        if (googleMap!=null && lastLatLng != null){
            // Showing the current location in Google Map
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng, 15));
            String[] information = getInformationFromLocation(lastLatLng);
            // Marker option
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(lastLatLng);
            markerOptions.title(information[0]);
            markerOptions.snippet(information[1]);
            marker = googleMap.addMarker(markerOptions);

        }
    }

    public String[] getInformationFromLocation(LatLng position){
        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(position.latitude,position.longitude,1);
            return new String[]{addresses.get(0).getAddressLine(0),addresses.get(0).getFeatureName()};

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String[]{"",""};
    }

    // join list of point using line on the map
    public void addPolyline(List<LatLng> points){
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.addAll(points);
        polylineOptions.width(12);
        polylineOptions.color(Color.RED);
        polylineOptions.geodesic(true);
        if (googleMap != null){
            googleMap.addPolyline(polylineOptions);
        }
    }

}
