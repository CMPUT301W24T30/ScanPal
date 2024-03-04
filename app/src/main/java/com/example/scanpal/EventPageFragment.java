package com.example.scanpal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class EventPageFragment extends Fragment {

    // Get result of activity if qr code is scanned
    private final ActivityResultLauncher<Intent> qrCodeScanner = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        IntentResult qrCodeResult  = IntentIntegrator.parseActivityResult(result.getResultCode(), result.getData());

        if(qrCodeResult.getContents() != null) { // if code scanned properly


        } else {  // if code scan failed

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
        Button scan = view.findViewById(R.id.button_scan);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator intentIntegrator = new IntentIntegrator(getActivity());
                intentIntegrator.setOrientationLocked(true);
                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                qrCodeScanner.launch(intentIntegrator.createScanIntent());
            }
        });
        return view;

        //just for testing linking to details page

        ListView eventList = view.findViewById(R.id.event_List);
        ArrayList<String> testList = new ArrayList<>();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(),R.layout.list_layout,R.id.textView_event,testList);//ArrayAdapter<>(this,,testList) ;

        //hardcoded for testing
        testList.add("test item 1");
        testList.add("test item  2");
        eventList.setAdapter(adapter);


        eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                NavController navController = NavHostFragment.findNavController(EventPageFragment.this);
                navController.navigate(R.id.select_event);
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
