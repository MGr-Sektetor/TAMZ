package com.example.snake_tamz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

public class MenuActivity extends Activity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setListeners();
       // if (!fingerprintHandler.getAuthenticated()) setAuthentication();

    }

    private void setListeners(){
        Button playButton = findViewById(R.id.playButton);
        playButton.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, MainActivity.class);
            startActivity(intent);
        });

       /* Button levelButton = findViewById(R.id.settings);
        levelButton.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, LevelSelectionActivity.class);
            startActivity(intent);
        });

        Button highscoreButton = findViewById(R.id.highscoreButton);
        highscoreButton.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, HighscoreActivity.class);
            startActivity(intent);
        });*/
    }
}