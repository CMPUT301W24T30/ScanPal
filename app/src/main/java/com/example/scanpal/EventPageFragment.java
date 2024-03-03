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

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;

public class EventPageFragment extends Fragment {

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
