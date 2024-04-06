package com.example.scanpal;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.scanpal.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    protected FirebaseFirestore db = FirebaseFirestore.getInstance();
    private GoogleMap mMap;
    private ActivityMapsBinding binding;

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

            String toastText = "";

            if ("eventLocation".equals(marker.getTag())) {
                // For event markers, set the toast text to the event's name
                toastText = marker.getTitle() != null ? marker.getTitle() : "Event location";
            } else {
                // For attendee markers, set the toast text to the attendee's name
                toastText = marker.getTitle() != null ? marker.getTitle() : "Attendee";
                marker.showInfoWindow();
            }

            // Only show the toast if we have non-null text
            if (!toastText.isEmpty()) {
                Toast.makeText(MapsActivity.this, toastText, Toast.LENGTH_SHORT).show();
            }

            return true;
        });
    }

    private void addMarkerToMap(LatLng latLng, String title) {
        if (mMap != null) {
            MarkerOptions options = new MarkerOptions().position(latLng).title(title);
            mMap.addMarker(options);
        }
    }

    private void addEventMarkerToMap(LatLng latLng, String title) {
        if (mMap != null) {
            BitmapDescriptor blueColor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
            MarkerOptions options = new MarkerOptions().position(latLng).title(title).icon(blueColor);
            Marker marker = mMap.addMarker(options);
            marker.setTag("eventLocation");
        }
    }


    private void fetchEventAndAttendeeLocations() {
        String eventId = getIntent().getStringExtra("event_id");
        DocumentReference eventRef = db.collection("Events").document(eventId);

        // Fetch the event details to get the location name for geocoding
        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String locationCoords = documentSnapshot.getString("locationCoords");
                if (locationCoords != null && !locationCoords.isEmpty()) {
                    String[] latLng = locationCoords.split(",");
                    if (latLng.length == 2) {
                        try {
                            double latitude = Double.parseDouble(latLng[0]);
                            double longitude = Double.parseDouble(latLng[1]);
                            LatLng eventLocation = new LatLng(latitude, longitude);
                            addEventMarkerToMap(eventLocation, documentSnapshot.getString("name"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 15));
                        } catch (NumberFormatException e) {
                            Log.e("MapsActivity", "Failed to parse event locationCoords string: " + locationCoords, e);
                        }
                    }
                } else {
                    Log.d("MapsActivity", "Event locationCoords is null or empty");
                }
            } else {
                Log.d("MapsActivity", "No event found with ID: " + eventId);
            }
        }).addOnFailureListener(e -> Log.e("MapsActivity", "Error fetching event details", e));


        db.collection("Attendees")
                .whereEqualTo("eventID", eventRef)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        DocumentReference userRef = documentSnapshot.getDocumentReference("user");
                        if (userRef != null) {
                            userRef.get().addOnSuccessListener(userSnapshot -> {
                                if (userSnapshot.exists()) {
                                    String firstName = userSnapshot.getString("firstName");
                                    String lastName = userSnapshot.getString("lastName");

                                    String fullName = "";
                                    if (firstName != null && lastName != null) {
                                        fullName = firstName + " " + lastName;
                                    } else if (firstName != null) {
                                        fullName = firstName;
                                    } else if (lastName != null) {
                                        fullName = lastName;
                                    } else {
                                        // Fallback in case both names are null
                                        fullName = "Attendee";
                                    }

                                    String photoUrl = userSnapshot.getString("photo");
                                    String locationString = documentSnapshot.getString("location");
                                    if (locationString != null && !locationString.isEmpty()) {
                                        String[] latLng = locationString.split(",");
                                        if (latLng.length == 2) {
                                            try {
                                                double latitude = Double.parseDouble(latLng[0]);
                                                double longitude = Double.parseDouble(latLng[1]);
                                                LatLng latLngObj = new LatLng(latitude, longitude);
                                                MarkerOptions markerOptions = new MarkerOptions()
                                                        .position(latLngObj)
                                                        .title(fullName != null ? fullName : "Username Not Found");
                                                Marker attendeeMarker = mMap.addMarker(markerOptions);
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
}