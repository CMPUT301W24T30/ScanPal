package com.example.scanpal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class EventPageFragment extends Fragment {


    // Get result of activity if qr code is scanned
    ActivityResultLauncher<ScanOptions> qrCodeScanner = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            QrScannerController.handleResult(result.getContents());
        } else {
            Toast.makeText(getContext(), "Invalid QR Code", Toast.LENGTH_SHORT).show();
        }
    });

    FloatingActionButton addEventButton;

    /**
     * empty default constructor
     */
    public EventPageFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.events_page, container, false);

        // Qr Code Check in
        FloatingActionButton scan = view.findViewById(R.id.button_scan);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScanOptions options = QrScannerController.getOptions();
                qrCodeScanner.launch(options);
            }
        });

        return view;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        addEventButton = view.findViewById(R.id.button_add_event);

        addEventButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventPageFragment.this);
            navController.navigate(R.id.addEvent);
        });

    }
    

}
