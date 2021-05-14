package com.example.whereismycar;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    List<Location> savedLocations;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    private String latSaved;
    private String lonSaved;
    private TextView savedText;
    LatLng latLng;
    LatLng lastLocationPlaced;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        MyApplication myApplication = (MyApplication) getApplicationContext();
        savedLocations = myApplication.getMyLocations();

        savedText = (TextView) findViewById(R.id.savedText);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng sydney = new LatLng(-34, 151);
        lastLocationPlaced = sydney;

        loadData();
        addMarker();
        onMarkerClick();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);


    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        try {
            latSaved = sharedPreferences.getString(LAT, "");
            lonSaved = sharedPreferences.getString(LON, "");
            Double latSavedDouble = Double.parseDouble(latSaved);
            Double lonSavedDouble = Double.parseDouble(lonSaved);
            latLng = new LatLng(latSavedDouble, lonSavedDouble);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(this, "Wystąpił problem z zapisaniem lokalizacji", Toast.LENGTH_SHORT).show();
        }
    }

    public void addMarker() {
        try {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Lat: " + latSaved + " Lon: " + lonSaved);
            mMap.addMarker(markerOptions);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Wystąpił problem z zapisaniem lokalizacji", Toast.LENGTH_SHORT).show();
        }
    }

    public void onMarkerClick() {
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                Integer clicks = (Integer) marker.getTag();
                if (clicks == null) {
                    clicks = 0;
                }
                clicks++;
                marker.setTag(clicks);
                Toast.makeText(MapsActivity.this,
                        "Marker " + marker.getTitle() + " was clicked " + marker.getTag() + " times.",
                        Toast.LENGTH_SHORT).show();


                return false;
            }
        });
    }


}