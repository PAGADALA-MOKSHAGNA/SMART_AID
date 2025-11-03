package com.example.smartfirstaid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartfirstaid.data.db.MongoHelper;    // <-- your helper
import com.example.smartfirstaid.util.PasswordUtils;     // <-- PBKDF2 utils

import org.bson.Document;

import java.util.Arrays;
import java.util.Date;

public class SigninActivity extends AppCompatActivity {

    private Button signin;
    private EditText mobno, password;

    private static final String PREFS = "SmartFirstAidPrefs";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_AGE = "age";
    private static final String KEY_PHONE_NUMBER = "phone_number";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.signin_activity);

        signin   = findViewById(R.id.signin);
        mobno    = findViewById(R.id.mobileNumber);
        password = findViewById(R.id.password);

        signin.setOnClickListener(v -> attemptSignin());
    }

    private void attemptSignin() {
        String phone = mobno.getText().toString().trim();
        String pass  = password.getText().toString();

        if (phone.isEmpty() || pass.isEmpty()) {
            toastBottom("Enter phone and password");
            return;
        }
        new SignInTask(phone, pass).execute();
    }

    private class SignInTask extends AsyncTask<Void, Void, String> {
        private final String phone, pass;
        private String username; // to pass to HomeActivity
        private int age;

        SignInTask(String phone, String pass) {
            this.phone = phone;
            this.pass  = pass;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // Fetch minimal fields needed for auth + display name
                Document user = MongoHelper.userDetails()
                        .find(new Document("phone", phone))
                        .projection(new Document("_id", 0)
                                .append("firstName", 1)
                                .append("lastName", 1)
                                .append("passwordHash", 1)
                                .append("passwordSalt", 1)
                                .append("age",1)
                        )
                        .first();

                if (user == null) return "User not found";

                username = (user.getString("firstName") != null ? user.getString("firstName") : "")
                        + ((user.getString("lastName") != null && !user.getString("lastName").isEmpty())
                        ? " " + user.getString("lastName") : "");
                age = user.getInteger("age");

                byte[] salt        = PasswordUtils.fromBase64(user.getString("passwordSalt"));
                byte[] storedHash  = PasswordUtils.fromBase64(user.getString("passwordHash"));
                byte[] givenHash   = PasswordUtils.pbkdf2(pass.toCharArray(), salt, 150_000, storedHash.length);

                if (!Arrays.equals(storedHash, givenHash)) return "Invalid password";

                // Optional: update lastLoginAt
                MongoHelper.userDetails().updateOne(
                        new Document("phone", phone),
                        new Document("$set", new Document("lastLoginAt", new Date()))
                );

                return "OK";
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String res) {
            if ("OK".equals(res)) {
                SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
                prefs.edit()
                        .putBoolean("isLoggedIn", true)
                        .putString(KEY_NAME,username)
                        .putInt(KEY_AGE, age)
                        .putString(KEY_PHONE_NUMBER, phone)
                        .apply();

                Intent intent = new Intent(SigninActivity.this, HomeActivity.class);
                if (username != null && !username.trim().isEmpty()) {
                    intent.putExtra("username", username.trim());
                }
                startActivity(intent);
                finish();
            } else {
                toastBottom(res.equals("User not found") || res.equals("Invalid password")
                        ? "Invalid credentials. Please try again."
                        : res);
                mobno.setText("");
                password.setText("");
                mobno.requestFocus();
            }
        }
    }

    private void toastBottom(String msg) {
        Toast t = Toast.makeText(SigninActivity.this, msg, Toast.LENGTH_SHORT);
        t.setGravity(Gravity.BOTTOM, 0, 100);
        t.show();
    }
}