package com.example.scanpal;

import java.util.ArrayList;

public interface EventFetchByUserCallback {

   void onSuccess(ArrayList<Event> eventList);
   void onError(Exception e);
}
