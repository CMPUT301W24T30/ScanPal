package com.example.scanpal;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;

public class EventPageFragment extends Fragment {

    FloatingActionButton addEventButton;
    ArrayList<String> testList;
    ArrayList<String> EventIDs;


    /**
     * empty default constructor
     */
    public EventPageFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.events_page, container, false);


        //just for testing linking to details page

        ListView eventList = view.findViewById(R.id.event_List);
        testList = new ArrayList<>();
        EventIDs = new ArrayList<>();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(),R.layout.list_layout,R.id.textView_event,testList);//ArrayAdapter<>(this,,testList) ;

        //hardcoded for testing
        //testList.add("test item 1");
        //testList.add("test item  2");


        EventController eventController = new EventController();
        ArrayList<Event> eventsList1;// = eventController.getEventsByUser(view);
        Log.d("EVENTPAGE", "BEFORE GET");
        eventController.getEventsByUser(view, new EventFetchByUserCallback() {
            @Override
            public void onSuccess(ArrayList<Event> eventsList) {
                //eventsList1 = eventList;
                testList.clear();//doesn't empty otherwise
                EventIDs.clear();

                for(int i = 0; i < eventsList.size(); i++) {
                    testList.add( eventsList.get(i).getName().toString());//gets listed by recent access
                    EventIDs.add( eventsList.get(i).getId() );

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

        //testList.add( eventsList.get(0).getName() );
        //testList.add( eventsList.get(1).getName() );


        //eventList.setAdapter(adapter);

       // listEvents(view);
        Log.d("EVENTPAGE", "RETURNED");



        eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                NavController navController = NavHostFragment.findNavController(EventPageFragment.this);
                Bundle bundle = new Bundle();
                bundle.putString("0", EventIDs.get(position));
                Log.d("BUNDLEVAL", EventIDs.get(position));
                navController.navigate(R.id.select_event, bundle );
            }
        });



        //stuff for adding an event
        addEventButton = view.findViewById(R.id.button_add_event);


        addEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = NavHostFragment.findNavController(EventPageFragment.this);
                navController.navigate(R.id.addEvent);
            }
        });



        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        /*
        addEventButton = view.findViewById(R.id.button_add_event);

        addEventButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventPageFragment.this);
            navController.navigate(R.id.addEvent);
        });*/

    }

}
