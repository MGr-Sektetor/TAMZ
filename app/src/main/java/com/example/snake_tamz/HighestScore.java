package com.example.snake_tamz;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class HighestScore extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highestscore);
        setListeners();
        //TextView text = findViewById(R.id.highscoreText2);
       // text.setText(String.valueOf(loadHighscoreFromPreferences()));
    }

    public int loadHighscoreFromPreferences(){
        SharedPreferences sharedPreferences = this.getSharedPreferences(MainActivity.SHARED_PREFERENCES, MODE_PRIVATE);
        return sharedPreferences.getInt(MainActivity.HIGHSCORE, 0);
    }

    public void setListeners(){
        Button highscoreBackButton = findViewById(R.id.highestscoreButton);
        highscoreBackButton.setOnClickListener(v -> {
            Intent intent = new Intent(HighestScore.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

}
