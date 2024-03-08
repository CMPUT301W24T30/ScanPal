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
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;

public class EventPageFragment extends Fragment {

    FloatingActionButton addEventButton;
    ArrayList<String> testList;
    ArrayList<String> EventIDs;
    ActivityResultLauncher<ScanOptions> qrCodeScanner;

    /**
     * empty default constructor
     */
    public EventPageFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.events_page, container, false);

        // QR Code Scanner initialization
        qrCodeScanner = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                QrScannerController.handleResult(result.getContents());
            } else {
                Toast.makeText(getContext(), "Invalid QR Code", Toast.LENGTH_SHORT).show();
            }
        });

        // QR Code Scan button setup
        FloatingActionButton scan = view.findViewById(R.id.button_scan);
        scan.setOnClickListener(v -> {
            ScanOptions options = QrScannerController.getOptions();
            qrCodeScanner.launch(options);
        });

        // Event List setup
        ListView eventList = view.findViewById(R.id.event_List);
        testList = new ArrayList<>();
        EventIDs = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), R.layout.list_layout, R.id.textView_event,
                testList);

        // Fetch events
        EventController eventController = new EventController();
        eventController.getEventsByUser(view, new EventFetchByUserCallback() {
            @Override
            public void onSuccess(ArrayList<Event> eventsList) {
                testList.clear();
                EventIDs.clear();
                for (int i = 0; i < eventsList.size(); i++) {
                    testList.add(eventsList.get(i).getName().toString());
                    EventIDs.add(eventsList.get(i).getId());
                    Log.d("EVENTPAGENAMES", eventsList.get(i).getName().toString());
                }
                Log.d("eventSIZEPAGE", Integer.toString(testList.size()));
                eventList.setAdapter(adapter);
            }

            @Override
            public void onError(Exception e) {
                Log.d("EVENTPAGENAMES", "ERROR");
            }
        });

        // Event click listener
        eventList.setOnItemClickListener((parent, item, position, id) -> {
            NavController navController = NavHostFragment.findNavController(EventPageFragment.this);
            Bundle bundle = new Bundle();
            bundle.putString("0", EventIDs.get(position));
            Log.d("BUNDLEVAL", EventIDs.get(position));
            navController.navigate(R.id.select_event, bundle);
        });

        // Add Event button setup
        addEventButton = view.findViewById(R.id.button_add_event);
        addEventButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventPageFragment.this);
            navController.navigate(R.id.addEvent);
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // No additional setup needed here
    }
}
