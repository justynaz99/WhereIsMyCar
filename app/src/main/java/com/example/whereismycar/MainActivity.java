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

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FAST_UPDATE_INTERVAL = 5;
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch sw_locationUpdates, sw_savePower;
    Button btn_savePlace, btn_showAddress, btn_showMap;

    //variable to remember if you are tracking location or not
    boolean updateOn = false;

    Location currentLocation;
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

        sw_savePower = findViewById(R.id.sw_savePower);
        sw_locationUpdates = findViewById(R.id.sw_locationUpdates);
        btn_savePlace = findViewById(R.id.btn_savePlace);
        btn_showAddress = findViewById(R.id.btn_showAddress);
        btn_showMap = findViewById(R.id.btn_showMap);

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
                updateUIValues(locationResult.getLastLocation());
            }
        };

        btn_savePlace.setOnClickListener(new View.OnClickListener() {
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

        btn_showAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i =  new Intent(MainActivity.this, ShowSavedLocationsList.class);
                startActivity(i);
            }
        });

        btn_showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(i);
            }
        });

        sw_savePower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_savePower.isChecked()) {
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                } else {
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                }
            }
        });

        sw_locationUpdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_locationUpdates.isChecked()) {
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

        try {
            double lat =  currentLocation.getLatitude();
            double lon = currentLocation.getLongitude();
            editor.putString(LAT, Double.toString(lat));
            editor.putString(LON, Double.toString(lon));
            editor.apply();
            Toast.makeText(this, "Miejsce zapisane!", Toast.LENGTH_SHORT).show();
        } catch (NullPointerException e) {
            e.printStackTrace();
            Toast.makeText(this, "Nieznana lokalizacja", Toast.LENGTH_SHORT).show();
        }

    }



    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        updateGPS();
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateGPS();
            } else {
                Toast.makeText(this, "This app requires permission to be granted in order to work properly",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
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
        Geocoder geocoder = new Geocoder(MainActivity.this);

        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (Exception e) {
//            tv_address.setText(R.string.unable_adress);
        }

        MyApplication myApplication = (MyApplication)getApplicationContext();
        savedLocations = myApplication.getMyLocations();
        

    }
}