package com.example.whereismycar;

import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class ShowSavedLocationsList extends AppCompatActivity {


    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    private TextView tv_address;
    List<Address> addresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_saved_locations_list);

        tv_address = (TextView) findViewById(R.id.savedText);

        loadData();
        updateViews();

    }

    public void loadData() {

        Geocoder geocoder = new Geocoder(ShowSavedLocationsList.this);

        try {
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            String latSaved = sharedPreferences.getString(LAT, "");
            String lonSaved = sharedPreferences.getString(LON, "");
            double latDouble = Double.parseDouble(latSaved);
            double lonDouble = Double.parseDouble(lonSaved);
            try {
                addresses = geocoder.getFromLocation(latDouble, lonDouble, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }





    }

    public void updateViews() {

        try {
            tv_address.setText(addresses.get(0).getAddressLine(0));
        } catch (Exception e) {
            tv_address.setText(R.string.unable_adress);
        }
    }
}