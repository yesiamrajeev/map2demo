package com.example.map2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Map2Activity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private Marker haryanaMarker;
    private Polyline currentPolyline;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map2);
        getLocationPermission();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);
        setupShowRouteButton();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng delhiLatLng = new LatLng(20.24811936715895, 85.80219702893264); // Delhi coordinates
        LatLng haryanaLatLng = new LatLng(20.24811936715895, 85.80219702893264); // Haryana coordinates
        LatLng gurugramLatLng = new LatLng(20.24879434926989, 85.80107513143706); // Gurugram coordinates

        MarkerOptions delhiMarkerOptions = new MarkerOptions()
                .position(delhiLatLng)
                .title("Delhi")
                .snippet("The capital of India");

        MarkerOptions haryanaMarkerOptions = new MarkerOptions()
                .position(haryanaLatLng)
                .title("Haryana")
                .snippet("Tap here to calculate directions");

        mMap.addMarker(delhiMarkerOptions);
        haryanaMarker = mMap.addMarker(haryanaMarkerOptions);

        // Set a click listener for the Haryana marker
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.equals(haryanaMarker)) {
                    // Handle the click on the Haryana marker
                    // Change the marker's appearance to indicate it's selected
                    haryanaMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    return true; // Consume the click event
                } else {
                    // Deselect any previously selected marker
                    if (haryanaMarker != null) {
                        haryanaMarker.setIcon(BitmapDescriptorFactory.defaultMarker());
                    }
                }
                return false; // Allow default behavior for other markers
            }
        });

        // Set a map click listener to deselect the marker when tapping the map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // Deselect the selected marker
                if (haryanaMarker != null) {
                    haryanaMarker.setIcon(BitmapDescriptorFactory.defaultMarker());
                }
            }
        });

        // Move the camera to a suitable position (e.g., Delhi)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(delhiLatLng, 10));
    }

    private void getLocationPermission() {
        // Check and request location permissions
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initMap();
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, initialize the map
                initMap();
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupShowRouteButton() {
        Button showRouteButton = findViewById(R.id.showRouteButton);
        showRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (haryanaMarker != null) {
                    // Handle the button click to show the route from Delhi to Haryana
                    LatLng delhiLatLng = new LatLng(20.249243, 85.801250); // Delhi coordinates
                    LatLng haryanaLatLng = haryanaMarker.getPosition();
                    calculateDirections(delhiLatLng, haryanaLatLng);
                } else {
                    Toast.makeText(Map2Activity.this, "Please select a marker first", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void calculateDirections(LatLng origin, LatLng destination) {
        // Create a PolylineOptions object for the route
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(origin) // Add the origin point
                .add(new LatLng(20.248613194060958, 85.80107753354918)) // Add Gurugram as a waypoint
                .add(new LatLng(20.248586351874835, 85.80093238856625))
                .add(new LatLng(20.247978894192027, 85.80091484356832))
                .add(destination) // Add the final destination
                .color(ContextCompat.getColor(this, R.color.routeColor)) // Customize the color
                .width(10); // Customize the width

        // Add the polyline to the map
        if (currentPolyline != null) {
            currentPolyline.remove(); // Remove previous polyline if exists
        }
        currentPolyline = mMap.addPolyline(polylineOptions);

        // Hide the marker for Gurugram
        if (haryanaMarker != null) {
            haryanaMarker.setVisible(false);
        }
    }



    private class FetchDirectionsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                // Make an HTTP request and get the JSON response
                String url = urls[0];
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                // Parse the JSON response and extract the route information
                // You can use a JSON parsing library like Gson for this
                // For brevity, I'm skipping the parsing code here

                // Once you have the route information, draw it on the map
                // Create a PolylineOptions object and add points to it
                PolylineOptions polylineOptions = new PolylineOptions();
                // Add the route points here

                // Customize the polyline appearance as needed
                polylineOptions.color(ContextCompat.getColor(Map2Activity.this, R.color.routeColor));
                polylineOptions.width(10);

                // Add the polyline to the map
                if (currentPolyline != null) {
                    currentPolyline.remove(); // Remove previous polyline if exists
                }
                currentPolyline = mMap.addPolyline(polylineOptions);
            } else {
                // Handle the case where there was an error fetching directions
                Toast.makeText(Map2Activity.this, "Error fetching directions", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
