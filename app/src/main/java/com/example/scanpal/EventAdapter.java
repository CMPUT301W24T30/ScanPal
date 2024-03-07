package com.example.scanpal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;


public class EventAdapter extends ArrayAdapter<Event> {
    public EventAdapter(Context context, List<Event> events) {
        super(context, 0, events);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item_event, parent, false);
        }

        Event event = getItem(position);
        TextView titleView = convertView.findViewById(R.id.event_title);
        ImageView thumbnailView = convertView.findViewById(R.id.event_thumbnail);

        titleView.setText(event.getName());
        // Set the thumbnail image
        // If your event objects hold a drawable resource ID or a filename in the drawable folder:
        thumbnailView.setImageResource(R.drawable.default_event_thumbnail); // Default or event-specific thumbnail

        return convertView;
    }
}

