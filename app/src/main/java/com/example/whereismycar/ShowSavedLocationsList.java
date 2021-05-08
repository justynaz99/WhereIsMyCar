package com.example.whereismycar;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class ShowSavedLocationsList extends AppCompatActivity {

    ListView lv_savedLocations;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    private String latToSave;
    private String lonToSave;
    private TextView savedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_saved_locations_list);


        MyApplication myApplication = (MyApplication)getApplicationContext();
        List<Location> savedLocations = myApplication.getMyLocations();

        savedText = (TextView) findViewById(R.id.savedText);

        loadData();
        updateViews();



    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        latToSave = sharedPreferences.getString(LAT, "");
        lonToSave = sharedPreferences.getString(LON, "");
    }

    public void updateViews() {
        savedText.setText(latToSave + " " + lonToSave);
    }
}