package com.example.scanpal;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import android.widget.GridView;
import java.util.List;
import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.gms.tasks.OnCompleteListener;




/**
 * Fragment for displaying a list of events. Allows users to navigate to event details,
 * add new events, and scan QR codes for event-related actions.
 */
public class EventPageFragment extends Fragment {

    private ArrayList<String> testList;
    private ArrayList<String> EventIDs;
    private ActivityResultLauncher<ScanOptions> qrCodeScanner;
    private QrScannerController qrScannerController;
    private GridView gridView;
    private ArrayAdapter<String> adapter;
    private List<String> eventNames = new ArrayList<>();
    private EventController eventController;

    FloatingActionButton addEventButton;
    /**
     * empty default constructor
     */
    public EventPageFragment() {
        // Required empty public constructor
    }

    @Override
public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.events_page, container, false);

    // QR Code scanning setup
    AttendeeController attendeeController = new AttendeeController(FirebaseFirestore.getInstance(), getContext());
    qrScannerController = new QrScannerController(attendeeController);
    qrCodeScanner = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            UserController userController = new UserController(FirebaseFirestore.getInstance(), getContext());
            String username = userController.fetchStoredUsername();
            qrScannerController.handleResult(result.getContents(), username);
        } else {
            Toast.makeText(getContext(), "Invalid QR Code", Toast.LENGTH_SHORT).show();
        }
    });
    FloatingActionButton scan = view.findViewById(R.id.button_scan);
    scan.setOnClickListener(v -> qrCodeScanner.launch(QrScannerController.getOptions()));

    // Initialize GridView for events
    gridView = view.findViewById(R.id.event_List);
    List<Event> events = new ArrayList<>();
    EventAdapter adapter = new EventAdapter(view.getContext(), events);
    gridView.setAdapter(adapter);

    // Fetching the list of events from Firestore
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    db.collection("Events").get().addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
            events.clear(); // Clear existing events
            for (QueryDocumentSnapshot document : task.getResult()) {
                String name = document.getString("name");
                String imageUrl = document.getString("imageUrl");
                String description = document.getString("description") != null ? document.getString("description") : "No description"; 

                // Create a temporary or null User object if not required for display
                Event event = new Event(null, name, description, imageUrl);
                event.setId(document.getId()); // Set the document ID as the event ID
                events.add(event);
            }
            adapter.notifyDataSetChanged();
        } else {
            Log.d("EventPageFragment", "Error getting documents: ", task.getException());
        }
    });

    gridView.setOnItemClickListener((parent, item, position, id) -> {
        NavController navController = NavHostFragment.findNavController(EventPageFragment.this);
        Bundle bundle = new Bundle();
        bundle.putString("eventId", events.get(position).getId());
        navController.navigate(R.id.select_event, bundle);
    });

    // Set up button to add new events
    FloatingActionButton addEventButton = view.findViewById(R.id.button_add_event);
    addEventButton.setOnClickListener(v -> {
        NavController navController = NavHostFragment.findNavController(EventPageFragment.this);
        navController.navigate(R.id.addEvent);
    });

    // Navigate to user profile
    FloatingActionButton profileButton = view.findViewById(R.id.button_profile);
    profileButton.setOnClickListener(v -> {
        NavController navController = NavHostFragment.findNavController(EventPageFragment.this);
        navController.navigate(R.id.events_to_profile); // Ensure this ID matches your navigation action ID
    });

    return view;
}


}
