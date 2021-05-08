package com.example.whereismycar;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FAST_UPDATE_INTERVAL = 5;
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);

    TextView tv_lat, tv_lon, tv_sensor, tv_updates, tv_address, tv_wayPointsCount, savedText;
    Switch sw_locationupdates, sw_gps;
    Button button_newWayPoint, button_showWayPointList, button_showMap;

    //variable to remember if you are tracking location or not
    boolean updateOn = false;

    //current location
    Location currentLocation;
    //list of saved locations
    List<Location> savedLocations;

    //Google's API for location services
    FusedLocationProviderClient fusedLocationProviderClient;
    //Location request is a config file for all settings related to FusedLocationProviderClient
    LocationRequest locationRequest;
    LocationCallback locationCallBack;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    private String latToSave;
    private String lonToSave;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        sw_gps = findViewById(R.id.sw_gps);
        sw_locationupdates = findViewById(R.id.sw_locationsupdates);
        button_newWayPoint = findViewById(R.id.button_newWayPoint);
        button_showWayPointList = findViewById(R.id.button_showWayPointList);
        tv_wayPointsCount = findViewById(R.id.tv_waypoints);
        button_showMap = findViewById(R.id.button_showMap);



        locationRequest = new LocationRequest();
        //how often app updates
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);
        //something with battery
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                //save the location
                updateUIValues(locationResult.getLastLocation());
            }
        };

        button_newWayPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get the GPS location

                //add the new location to the global list
                MyApplication myApplication = (MyApplication) getApplicationContext();
                savedLocations = myApplication.getMyLocations();
                savedLocations.add(currentLocation);

                saveData();
            }
        });

        button_showWayPointList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i =  new Intent(MainActivity.this, ShowSavedLocationsList.class);
                startActivity(i);
            }
        });

        button_showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(i);
            }
        });

        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_gps.isChecked()) {
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    tv_sensor.setText("Using GPS sensors");
                } else {
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("Using towers + WIFI");
                }
            }
        });

        sw_locationupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_locationupdates.isChecked()) {
                    startLocationUpdates();
                } else {
                    stopLocationUpdates();
                }
            }
        });


        updateGPS();
    }

    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Double lat =  currentLocation.getLatitude();
        Double lon = currentLocation.getLongitude();
        editor.putString(LAT, lat.toString());
        editor.putString(LON, lon.toString());
        editor.apply();

        Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show();
    }





    private void startLocationUpdates() {
        tv_updates.setText("Location is being tracked");
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
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        updateGPS();
    }

    private void stopLocationUpdates() {
        tv_updates.setText("Location is not being tracked");
        tv_lat.setText("Location is not being tracked");
        tv_lon.setText("Location is not being tracked");
        tv_address.setText("Location is not being tracked");
        tv_sensor.setText("Location is not being tracked");

        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS();
                } else {
                    Toast.makeText(this, "This app requires permission to be granted in order to work properly",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    private void updateGPS() {
        //get permissions from the user to track GPS
        //get the current location from the fused client
        //update the UI - set all properties in their associated text views

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //user provided the permission

            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    //we got permissions. Put the values of location into the UI components
                    if (location != null) {
                        updateUIValues(location);
                        currentLocation = location;
                    }
                }
            });

        } else {
            //permission not granted yet
            //if we have right version of Android
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }

    }

    private void updateUIValues(Location location) {
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));

        Geocoder geocoder = new Geocoder(MainActivity.this);

        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            tv_address.setText(addresses.get(0).getAddressLine(0));
        } catch (Exception e) {
            tv_address.setText("Unable to get street address");
        }

        MyApplication myApplication = (MyApplication)getApplicationContext();
        savedLocations = myApplication.getMyLocations();

        //show the number of waypoints saved
        tv_wayPointsCount.setText(Integer.toString(savedLocations.size()));

    }
}
