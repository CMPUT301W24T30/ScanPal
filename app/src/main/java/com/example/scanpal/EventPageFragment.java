package com.example.scanpal;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


import java.util.ArrayList;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;




public class EventPageFragment extends Fragment {
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> eventNames = new ArrayList<>();
    private EventController eventController;

    FloatingActionButton addEventButton;

    public EventPageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.events_page, container, false);
        ListView eventList = view.findViewById(R.id.event_List);
        ArrayList<String> eventNames = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), R.layout.list_layout, R.id.textView_event, eventNames);
        eventList.setAdapter(adapter);

        // Fetching the list of events from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Events").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String name = document.getString("name"); // Make sure "name" matches the field name in your Firestore document
                        eventNames.add(name);
                    }
                    // Notify the adapter that the dataset has changed so the ListView will update
                    adapter.notifyDataSetChanged();
                } else {
                    Log.d("EventPageFragment", "Error getting documents: ", task.getException());
                }
            }
        });

        eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Here you would handle the list item click to navigate to the event details
                // You'll likely need to pass the event ID or other relevant data to the details fragment
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

