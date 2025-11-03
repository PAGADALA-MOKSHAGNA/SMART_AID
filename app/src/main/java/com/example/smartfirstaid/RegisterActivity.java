package com.example.smartfirstaid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartfirstaid.data.db.MongoHelper;       // <-- make sure package matches your helper
import com.example.smartfirstaid.util.PasswordUtils;        // <-- PBKDF2 utils
import com.mongodb.client.MongoCollection;

import org.bson.Document;

import java.util.Date;

public class RegisterActivity extends AppCompatActivity {

    Button register;
    EditText firstname, lastname, age, phone_number, password, confirm_password;
    RadioGroup gender;
    RadioButton male, female;

    private static final String usr_SHARED_PREFS = "SmartFirstAidPrefs";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_AGE = "age";
    private static final String KEY_PHONE_NUMBER = "phone_number";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.register_activity);

        register        = findViewById(R.id.register);
        firstname       = findViewById(R.id.firstname);
        lastname        = findViewById(R.id.lastname);
        age             = findViewById(R.id.age);
        gender          = findViewById(R.id.genderGroup);
        male            = findViewById(R.id.male);
        female          = findViewById(R.id.female);
        phone_number    = findViewById(R.id.mobileNumber);
        password        = findViewById(R.id.password);
        confirm_password= findViewById(R.id.confirmPassword);

        register.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String fn = firstname.getText().toString().trim();
        String ln = lastname.getText().toString().trim();
        String ageStr = age.getText().toString().trim();
        String phone = phone_number.getText().toString().trim();
        String pwd = password.getText().toString();
        String cpwd = confirm_password.getText().toString();

        // gender
        int checkedId = gender.getCheckedRadioButtonId();
        String genderStr = (checkedId == R.id.male) ? "Male" :
                (checkedId == R.id.female) ? "Female" : "";

        // basic validations
        if (fn.isEmpty() || ln.isEmpty() || ageStr.isEmpty() || genderStr.isEmpty()
                || phone.isEmpty() || pwd.isEmpty() || cpwd.isEmpty()) {
            toastBottom("Please fill all fields");
            return;
        }

        int ageVal;
        try {
            ageVal = Integer.parseInt(ageStr);
            if (ageVal < 5 || ageVal > 120) {
                toastBottom("Enter a valid age");
                return;
            }
        } catch (NumberFormatException e) {
            toastBottom("Age must be a number");
            return;
        }

        if (!pwd.equals(cpwd)) {
            toastBottom("Passwords do not match!");
            confirm_password.setText("");
            confirm_password.requestFocus();
            return;
        }

        // All good → proceed to DB insert off main thread
        new RegisterTask(fn, ln, ageVal, genderStr, phone, pwd).execute();
    }

    private class RegisterTask extends AsyncTask<Void, Void, String> {
        final String fn, ln, genderStr, phone, pwd;
        final int ageVal;

        RegisterTask(String fn, String ln, int ageVal, String genderStr, String phone, String pwd) {
            this.fn = fn; this.ln = ln; this.ageVal = ageVal;
            this.genderStr = genderStr; this.phone = phone; this.pwd = pwd;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                MongoCollection<Document> col = MongoHelper.userDetails(); // <-- your helper method

                // unique by phone check
                Document existing = col.find(new Document("phone", phone))
                        .projection(new Document("_id", 1))
                        .first();
                if (existing != null) return "User already exists with this phone number";

                // hash password
                byte[] salt = PasswordUtils.generateSalt(16);
                byte[] hash = PasswordUtils.pbkdf2(pwd.toCharArray(), salt, 150_000, 32);

                Document userDoc = new Document("firstName", fn)
                        .append("lastName", ln)
                        .append("age", ageVal)
                        .append("gender", genderStr)
                        .append("phone", phone)
                        .append("passwordHash", PasswordUtils.toBase64(hash))
                        .append("passwordSalt", PasswordUtils.toBase64(salt))
                        .append("createdAt", new Date())
                        .append("status", "active");

                col.insertOne(userDoc);
                return "OK";
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String res) {
            if ("OK".equals(res)) {
                // Save shared prefs and move to Home
                String username = fn + " " + ln;
                SharedPreferences prefs = getSharedPreferences(usr_SHARED_PREFS, MODE_PRIVATE);
                prefs.edit()
                        .putBoolean("isLoggedIn", true)
                        .putString(KEY_NAME, username)
                        .putInt(KEY_AGE, ageVal)
                        .putString(KEY_PHONE_NUMBER, phone)
                        .apply();

                toastBottom("Registration successful");
                startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                finish();
            } else {
                toastBottom(res);
            }
        }
    }

    private void toastBottom(String msg) {
        Toast t = Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT);
        t.setGravity(Gravity.BOTTOM, 0, 100);
        t.show();
    }
}