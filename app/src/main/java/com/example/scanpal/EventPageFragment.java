package com.example.scanpal;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import java.util.List;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.gms.tasks.OnCompleteListener;




public class EventPageFragment extends Fragment {
    private GridView gridView;
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
        gridView = view.findViewById(R.id.event_List); // Make sure this ID matches your GridView in events_page.xml
        List<Event> events = new ArrayList<>();
        EventAdapter adapter = new EventAdapter(view.getContext(), events);
        gridView.setAdapter(adapter);

        // Fetching the list of events from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Events").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    events.clear(); // Clear existing events
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Assuming Event class has a constructor that accepts a Firestore document.
                        // You might need to adjust this based on your Event class's constructor
                        Event event = new Event(document.getId(), document.getString("name"));
                        events.add(event);
                    }
                    // Notify the adapter that the dataset has changed so the GridView will update
                    adapter.notifyDataSetChanged();
                } else {
                    Log.d("EventPageFragment", "Error getting documents: ", task.getException());
                }
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Handle the grid item click here
                // Extract information from the event at 'position' if needed to pass to details fragment
                NavController navController = NavHostFragment.findNavController(EventPageFragment.this);
                // Assuming you have a method to get the event ID or any identifier
                String eventId = events.get(position).getId(); // Ensure your Event model has getId() method
                Bundle bundle = new Bundle();
                bundle.putString("eventId", eventId);
                navController.navigate(R.id.select_event, bundle);
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

