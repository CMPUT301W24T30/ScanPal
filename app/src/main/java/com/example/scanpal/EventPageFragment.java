package com.example.scanpal;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;

public class EventPageFragment extends Fragment {

    FloatingActionButton addEventButton, profileButton;
    ArrayList<String> testList;
    ArrayList<String> EventIDs;
    ActivityResultLauncher<ScanOptions> qrCodeScanner;
    private QrScannerController qrScannerController;

    /**
     * empty default constructor
     */
    public EventPageFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.events_page, container, false);

        AttendeeController attendeeController = new AttendeeController(FirebaseFirestore.getInstance(), getContext());
        qrScannerController = new QrScannerController(attendeeController);

        // Initialize QR Code Scanner
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

        // Event List setup
        ListView eventList = view.findViewById(R.id.event_List);
        testList = new ArrayList<>();
        EventIDs = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), R.layout.list_layout, R.id.textView_event, testList);

        // Fetch events
        EventController eventController = new EventController();
        eventController.getEventsByUser(view, new EventFetchByUserCallback() {
            @Override
            public void onSuccess(ArrayList<Event> eventsList) {
                testList.clear();
                EventIDs.clear();
                for (int i = 0; i < eventsList.size(); i++) {
                    testList.add(eventsList.get(i).getName());
                    EventIDs.add(eventsList.get(i).getId());
                }
                eventList.setAdapter(adapter);
            }

            @Override
            public void onError(Exception e) {
                Log.d("EVENT PAGE NAMES", "ERROR");
            }
        });

        // Event click listener
        eventList.setOnItemClickListener((parent, item, position, id) -> {
            NavController navController = NavHostFragment.findNavController(EventPageFragment.this);
            Bundle bundle = new Bundle();
            bundle.putString("0", EventIDs.get(position));
            navController.navigate(R.id.select_event, bundle);
        });

        // Add Event button setup
        addEventButton = view.findViewById(R.id.button_add_event);
        addEventButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventPageFragment.this);
            navController.navigate(R.id.addEvent);
        });

        profileButton = view.findViewById(R.id.button_profile);
        profileButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventPageFragment.this);
            navController.navigate(R.id.events_to_profile);
        });
        return view;
    }
}
