package in.blrobotics.blaengarrobotics;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONObject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GeofenceActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {

    private List<LatLng> points = new ArrayList<>();
    private Polygon polygon;
    private GoogleMap googleMap;
    Gson gson = new Gson();
    MySQLConnection conn;
    int userID;
    int deviceID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofence);
        /* Database connection */
        conn = new MySQLConnection(this);
        /* getSharedPreferences */
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preference_name), Context.MODE_PRIVATE);
        if (sharedPreferences.contains("userId")) {
            userID = sharedPreferences.getInt("userId", 0);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        SupportPlaceAutocompleteFragment autocompleteFragment = (SupportPlaceAutocompleteFragment)getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                googleMap.clear();
//                googleMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName().toString()));
                googleMap.addCircle(new CircleOptions()
                        .strokeWidth(5)
                        .radius(5000)
                        .center(place.getLatLng())
                        .strokeColor(Color.rgb(0, 50, 100))
                        .fillColor(Color.argb(20, 50, 0, 255))
                        .zIndex(55));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 12.0f));
            }

            @Override
            public void onError(Status status) {
                Toast toast= Toast.makeText(GeofenceActivity.this,status.toString(),Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_geofence);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insert();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        chooseDevice();
        googleMap.setOnMapClickListener(this);
        googleMap.setOnMarkerClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        // Marker option
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        googleMap.addMarker(markerOptions);
        points.add(latLng);
        drawPolygon();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        LatLng position = marker.getPosition();
        if (points.contains(position)) points.remove(position);
        marker.remove();
        if(!points.isEmpty())drawPolygon();
        return true;
    }

    public void drawPolygon(){
        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.addAll(points);
        polygonOptions.strokeWidth(5);
        polygonOptions.strokeColor(Color.argb(80, 50, 0, 255));
        polygonOptions.fillColor(0x7F00FF00);
        polygonOptions.zIndex(60);
        if (polygon != null)polygon.remove();
        polygon = googleMap.addPolygon(polygonOptions);
        if (!points.isEmpty())googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(points.get(points.size()-1), 12.0f));
    }

    public void insert(){
        String coordinates = gson.toJson(points);
        String query = "INSERT INTO `Geofences` (`id`, `device`, `coordinates`) VALUES (NULL, '"+deviceID+"', '"+coordinates+"') ON DUPLICATE KEY UPDATE `coordinates` = '"+ coordinates+"'";
        AsyncTask asyncTask = conn.execute(query);
        conn.setOnResult(new MySQLConnection.OnResult(asyncTask) {
            @Override
            void getResult(Object dataObject) throws Exception {
                if (dataObject != null){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(GeofenceActivity.this,"Successfully set geofence area",Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                }
            }
        });
    }

    public void chooseDevice(){
        String query = "SELECT `id`,`serial_no` FROM `Devices` WHERE `id` IN (SELECT `device` FROM `Owners` WHERE `user` = "+userID+")";
        AsyncTask asyncTask = conn.execute(query);
        conn.setOnResult(new MySQLConnection.OnResult(asyncTask) {
            @Override
            public void getResult(Object dataObject) throws Exception {
                JSONArray result = (JSONArray)dataObject;
                if (result != null){
                    if (!result.isNull(0)){
                        final List<String> deviceList = new ArrayList<>();
                        final List<Integer> idList = new ArrayList<>();
                        for (int i = 0; i < result.length(); i++) {
                            final JSONObject e = result.getJSONObject(i);
                            String name = e.getString("serial_no");
                            int id = e.getInt("id");
                            deviceList.add(name);
                            idList.add(id);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Showing alert dialog to choose a device
                                alertDiaglog(deviceList,idList);
                            }
                        });
                    }
                    else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast toast = Toast.makeText(GeofenceActivity.this, "You have no device", Toast.LENGTH_SHORT);
                                toast.show();
                                finish();
                            }
                        });
                    }
                }
                else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(GeofenceActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT);
                            toast.show();
                            finish();
                        }
                    });
                }
            }
        });
    }

    public void alertDiaglog(List<String> deviceList, final List<Integer> idList){
        // Create AutoCompleteTextView Dynamically
        final Spinner spinner = new Spinner(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        spinner.setLayoutParams(layoutParams);
        layoutParams.setMargins(30, 30, 30, 30);
        // Setting up spinner data
        /* Creating the instance of ArrayAdapter containing list of device serial no */
        ArrayAdapter<String> deviceListAdapter = new ArrayAdapter<String>(GeofenceActivity.this,android.R.layout.simple_spinner_dropdown_item, deviceList);
        // Adding spinner value
        spinner.setAdapter(deviceListAdapter);
        // Creating Alert dialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(spinner);
        alertDialogBuilder.setTitle("Choose a device");
        // Setting positive button
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                int index = spinner.getSelectedItemPosition();
                deviceID= idList.get(index);
                // Drawing a polygon for a particular device
                getGeofencesData();
            }
        });
        // Setting negative button
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                finish();
            }
        });
        // Get the alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        //Show the alert dialog
        alertDialog.show();
    }


    public void getGeofencesData(){
        String query = "SELECT `coordinates` FROM `Geofences` WHERE `device` = "+deviceID;
        AsyncTask asyncTask = conn.execute(query);
        conn.setOnResult(new MySQLConnection.OnResult(asyncTask) {
            @Override
            void getResult(Object dataObject) throws Exception {
                JSONArray result = (JSONArray)dataObject;
                if (!result.isNull(0)){
                    String pointList = result.getJSONObject(0).getString("coordinates");
                    Type type = new TypeToken<List<LatLng>>() {}.getType();
                    points = gson.fromJson(pointList, type);
                    if (!points.isEmpty()){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                drawPolygon();
                                for(LatLng pos:points){
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(pos);
                                    googleMap.addMarker(markerOptions);
                                }
                            }
                        });
                    }
                }
            }
        });
    }
}
