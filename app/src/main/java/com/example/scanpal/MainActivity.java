package com.example.scanpal;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //testing event details page
        setContentView(R.layout.event_details);
        //setContentView(R.layout.activity_main);
    }
}