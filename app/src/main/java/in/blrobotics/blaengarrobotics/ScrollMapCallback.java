package in.blrobotics.blaengarrobotics;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.preference.PreferenceManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class ScrollMapCallback implements OnMapReadyCallback {

    GoogleMap googleMap;
    Marker marker;
    List<LatLng> points;
    Context context;
    LatLng lastLatLng = null;

    public ScrollMapCallback(Context context, List<LatLng> points){
        this.points = points;
        this.context = context;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        // setting night mode on map
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("theme_switch", false)) {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.dark_map));
        }
        addPolyline(googleMap);
        setLocation(googleMap);
    }

    // set location to google maps
    public void setLocation(GoogleMap googleMap) {
        if (points != null && !points.isEmpty()){
            lastLatLng = points.get(points.size()-1);
        }
        // Add marker to the map
        if (marker != null){
            marker.remove();
        }
        if (googleMap!=null && lastLatLng != null){
            String[] information = getInformationFromLocation(lastLatLng);
            // Marker option
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(lastLatLng);
            markerOptions.title(information[0]);
            markerOptions.snippet(information[1]);
            marker = googleMap.addMarker(markerOptions);
            // Showing the current location in Google Map
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(lastLatLng));
            // Zoom in the Google Map
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }
    }

    public String[] getInformationFromLocation(LatLng position){
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(position.latitude,position.longitude,1);
            if (!addresses.isEmpty()){
                return new String[]{addresses.get(0).getAddressLine(0),addresses.get(0).getFeatureName()};
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String[]{"",""};
    }

    // join list of point using line on the map
    public void addPolyline(GoogleMap googleMap){
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.addAll(points);
        polylineOptions.width(12);
        polylineOptions.color(Color.RED);
        polylineOptions.geodesic(true);
        if (googleMap != null){
            googleMap.addPolyline(polylineOptions);
        }
    }

    public void appendData(List<LatLng> points){
        this.points = new ArrayList<>();
        this.points.add(lastLatLng);
        this.points.addAll(points);
        addPolyline(googleMap);
        setLocation(googleMap);
    }
}
