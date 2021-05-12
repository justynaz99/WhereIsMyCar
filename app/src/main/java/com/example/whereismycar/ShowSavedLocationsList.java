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


    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    private String lonSaved;
    private String latSaved;
    private TextView savedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_saved_locations_list);

        savedText = (TextView) findViewById(R.id.savedText);

        loadData();
        updateViews();

    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        latSaved = sharedPreferences.getString(LAT, "");
        lonSaved = sharedPreferences.getString(LON, "");
    }

    public void updateViews() {
        savedText.setText(latSaved + " " + lonSaved);
    }
}