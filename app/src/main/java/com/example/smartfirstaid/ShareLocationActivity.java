package com.example.smartfirstaid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ShareLocationActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 100;

    private FusedLocationProviderClient fusedLocationClient;
    private TextView tvLatitude, tvLongitude, tvAddress;
    private MaterialButton btnGetLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_location);

        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvAddress = findViewById(R.id.tvAddress);
        btnGetLocation = findViewById(R.id.btnGetLocation);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnGetLocation.setOnClickListener(v -> checkLocationPermissionAndFetch());
    }

    private void checkLocationPermissionAndFetch() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Ask for permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            getCurrentLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double lat = location.getLatitude();
                            double lon = location.getLongitude();

                            tvLatitude.setText("Latitude: " + lat);
                            tvLongitude.setText("Longitude: " + lon);

                            // Convert lat/lon to address
                            Geocoder geocoder = new Geocoder(ShareLocationActivity.this, Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                                if (addresses != null && !addresses.isEmpty()) {
                                    Address addr = addresses.get(0);
                                    String fullAddress = addr.getAddressLine(0);
                                    tvAddress.setText("Address: " + fullAddress);
                                } else {
                                    tvAddress.setText("Address: Not found");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                tvAddress.setText("Address: Unable to fetch");
                            }
                        } else {
                            Toast.makeText(ShareLocationActivity.this,
                                    "Unable to retrieve location. Please turn on GPS.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // Handle permission response
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
