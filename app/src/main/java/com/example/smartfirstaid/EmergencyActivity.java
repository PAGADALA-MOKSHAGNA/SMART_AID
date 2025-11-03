package com.example.smartfirstaid;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.SharedPreferences;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Button;
import android.content.Intent;

public class EmergencyActivity extends AppCompatActivity {

    private static final String usr_SHARED_PREFS = "SmartFirstAidPrefs";
    private TextView user_details;
    private LinearLayout snakebtn, burnbtn;
    private Button ShareLocationbtn;
    private static final String KEY_NAME = "user_name";
    private static final String KEY_AGE = "age";
    private static final String KEY_PHONE_NUMBER = "phone_number";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_emergency);

        user_details = (TextView) findViewById(R.id.userdetails);
        SharedPreferences prefs = getSharedPreferences(usr_SHARED_PREFS,MODE_PRIVATE);
        String name = prefs.getString(KEY_NAME, "null");
        int age = prefs.getInt(KEY_AGE, 0);
        String phone_number = prefs.getString(KEY_PHONE_NUMBER, "null");
        String userinfo = name + "\n" + "Age: " + age + "\n" + "Phone: +91" + phone_number;
        user_details.setText(userinfo);

        snakebtn = (LinearLayout) findViewById(R.id.btnSnakeBite);
        burnbtn = (LinearLayout) findViewById(R.id.btnBurns);

        snakebtn.setOnClickListener(v->{
            openDetail("snake_bite", "Snake Bite");
        });
        burnbtn.setOnClickListener(v->{
            openDetail("burns", "Burns");
        });
        ShareLocationbtn = (Button) findViewById(R.id.btnShareLocation);
        ShareLocationbtn.setOnClickListener(v -> {
            Intent intent = new Intent(EmergencyActivity.this, ShareLocationActivity.class);
            startActivity(intent);
        });
    }
    private void openDetail(String key, String title){
        Intent i = new Intent(this,EmergencyDetailActivity.class);
        i.putExtra("key", key);
        i.putExtra("title", title);
        startActivity(i);
    }
}