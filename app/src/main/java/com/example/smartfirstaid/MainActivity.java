package com.example.smartfirstaid;

import android.content.Intent;
import android.content.SharedPreferences; // needed to use SharedPreferences
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String usr_SHARED_PREFS = "SmartFirstAidPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // light-weight data storage mechanism (key - value pairs)
        SharedPreferences prefs =  getSharedPreferences(usr_SHARED_PREFS, MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn",false);

        if(isLoggedIn)
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
        else
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }
}