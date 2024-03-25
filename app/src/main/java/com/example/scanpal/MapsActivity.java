package com.example.scanpal;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.scanpal.databinding.ActivityMapsBinding;

import java.io.IOException;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private final View mWindow;

        public CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.info_window_layout, null);
        }

        private void renderWindowText(Marker marker, View view) {
            String imageUrl = (String) marker.getTag();
            ImageView infoWindowImage = view.findViewById(R.id.info_window_image);

            // Load the image with Glide and apply a circular transformation
            Glide.with(getApplicationContext())
                    .load(imageUrl)
                    .circleCrop()
                    .into(infoWindowImage);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            if ("eventLocation".equals(marker.getTag())) {
                View eventInfoWindow = getLayoutInflater().inflate(R.layout.event_info_window, null);
                TextView titleView = eventInfoWindow.findViewById(R.id.title);
                titleView.setText(marker.getTitle());
                return eventInfoWindow;
            } else {
                View attendeeInfoWindow = getLayoutInflater().inflate(R.layout.info_window_layout, null);
                renderWindowText(marker, attendeeInfoWindow);
                return attendeeInfoWindow;
            }
        }

        @Override
        public View getInfoContents(Marker marker) {
            if ("eventLocation".equals(marker.getTag())) {
                View view = getLayoutInflater().inflate(R.layout.event_info_window, null);
                TextView titleView = view.findViewById(R.id.title);
                titleView.setText(marker.getTitle());
                return view;
            } else {
                // For attendee markers, we could customize the contents as well if needed
                return null;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e("MapsActivity", "SupportMapFragment not found.");
        }

        // Find the FloatingActionButton and set a click listener to finish the activity
        FloatingActionButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            finish();
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        fetchEventAndAttendeeLocations();

        mMap.setOnMarkerClickListener(marker -> {
            // Trigger the info window to show
            marker.showInfoWindow();
            return true;
        });
    }

    private void addMarkerToMap(LatLng latLng, String title) {
        if (mMap != null) {
            MarkerOptions options = new MarkerOptions().position(latLng).title(title);
            mMap.addMarker(options);
        }
    }

    private void geocodeLocation(String locationName) {
        String apiKey = getString(R.string.google_maps_key); // Your API key

        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                // Use the address object to create a LatLng object
                LatLng eventLocation = new LatLng(address.getLatitude(), address.getLongitude());
                BitmapDescriptor eventIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE); // Custom color

                MarkerOptions eventMarkerOptions = new MarkerOptions()
                        .position(eventLocation)
                        .title(locationName)
                        .icon(eventIcon)
                        .zIndex(1.0f);

                Marker eventMarker = mMap.addMarker(eventMarkerOptions);
                eventMarker.setTag("eventLocation");

                mMap.addMarker(eventMarkerOptions);

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 15));
            } else {
                Log.e("MapActivity", "No address found for location: " + locationName);
            }
        } catch (IOException e) {
            Log.e("MapActivity", "Geocoder I/O exception", e);
        }
    }

    private void fetchEventAndAttendeeLocations() {
        String eventId = getIntent().getStringExtra("event_id");
        DocumentReference eventRef = db.collection("Events").document(eventId);

        // Fetch the event details to get the location name for geocoding
        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String eventLocationName = documentSnapshot.getString("location");
                if (eventLocationName != null && !eventLocationName.isEmpty()) {
                    geocodeLocation(eventLocationName);
                } else {
                    Log.d("MapActivity", "Event location name is null or empty");
                }
            } else {
                Log.d("MapActivity", "No event found with ID: " + eventId);
            }
        }).addOnFailureListener(e -> Log.e("MapActivity", "Error fetching event details", e));

        db.collection("Attendees")
                .whereEqualTo("eventID", eventRef)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        DocumentReference userRef = documentSnapshot.getDocumentReference("user");
                        if (userRef != null) {
                            userRef.get().addOnSuccessListener(userSnapshot -> {
                                if (userSnapshot.exists()) {
                                    String photoUrl = userSnapshot.getString("photo");
                                    String locationString = documentSnapshot.getString("location");
                                    if (locationString != null && !locationString.isEmpty()) {
                                        String[] latLng = locationString.split(",");
                                        if (latLng.length == 2) {
                                            try {
                                                double latitude = Double.parseDouble(latLng[0]);
                                                double longitude = Double.parseDouble(latLng[1]);
                                                LatLng latLngObj = new LatLng(latitude, longitude);
                                                Marker attendeeMarker = mMap.addMarker(new MarkerOptions().position(latLngObj).title(userSnapshot.getString("name"))); // Example, adjust as needed
                                                attendeeMarker.setTag(photoUrl);
                                            } catch (NumberFormatException e) {
                                                Log.e("MapsActivity", "Failed to parse location string: " + locationString, e);
                                            }
                                        }
                                    }
                                }
                            }).addOnFailureListener(e -> Log.e("MapsActivity", "Error fetching user details", e));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("MapsActivity", "Error fetching attendee locations", e));
    }
}