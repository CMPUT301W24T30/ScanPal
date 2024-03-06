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

        if(result.getContents() != null) { // if code scanned properly
            Toast.makeText(getContext(), result.getContents(), Toast.LENGTH_SHORT).show();

        } else {  // if code scan failed
            Toast.makeText(getContext(), "Flop", Toast.LENGTH_SHORT).show();
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
//                IntentIntegrator intentIntegrator = new IntentIntegrator(getActivity());
//                intentIntegrator.setOrientationLocked(true);
//                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
//                qrCodeScanner.launch(intentIntegrator.createScanIntent());
                ScanOptions options = new ScanOptions();
                options.setBeepEnabled(true);
                options.setOrientationLocked(true);
                options.setCaptureActivity(Capture.class);
                options.setPrompt("Place Qr Code inside the viewfinder");
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
