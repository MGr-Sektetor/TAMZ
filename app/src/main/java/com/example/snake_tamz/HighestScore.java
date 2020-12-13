package com.example.snake_tamz;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

public class HighestScore extends Activity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highestscore);
        setListeners();
        TextView text = findViewById(R.id.highestscoreText);
        text.setText(String.valueOf(loadHighscoreFromPreferences()));
        TextView text2 = findViewById(R.id.locationText);
        text2.setText(loadCurrentLocationFromPreferences());
    }

    public int loadHighscoreFromPreferences(){
        SharedPreferences sharedPreferences = this.getSharedPreferences(MainActivity.SHARED_PREFERENCES, MODE_PRIVATE);
        return sharedPreferences.getInt(MainActivity.HIGHSCORE, 0);
    }

    public String loadCurrentLocationFromPreferences(){


        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return "No location";
        }else {

            SharedPreferences sharedPreferences = this.getSharedPreferences(MainActivity.SHARED_PREFERENCES, MODE_PRIVATE);
            String lat = sharedPreferences.getString("LOCATION_LAT", null);
            String lon = sharedPreferences.getString("LOCATION_LON", null);

            Location location = null;
            if (lat != null && lon != null) {
                String provider = sharedPreferences.getString("LOCATION_PROVIDER", null);
                location = new Location(provider);
                location.setLatitude(Double.parseDouble(lat));
                location.setLongitude(Double.parseDouble(lon));
            }

            return String.valueOf(location);
        }

    }

    public void setListeners(){
        Button highestscoreBackButton = findViewById(R.id.highestscoreBackButton);
        highestscoreBackButton.setOnClickListener(v -> {
            Intent intent = new Intent(HighestScore.this, MenuActivity.class);
            startActivity(intent);
            finish();
        });
    }

}
