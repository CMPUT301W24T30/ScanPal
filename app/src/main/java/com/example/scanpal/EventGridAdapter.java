package com.example.scanpal;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import java.util.List;



public class EventGridAdapter extends BaseAdapter {
    private Context context;
    private List<Event> events;

    public EventGridAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Object getItem(int position) {
        return events.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.grid_item_event, parent, false);
        }

        Event event = events.get(position);

        ImageView imageView = convertView.findViewById(R.id.event_image);
        TextView textView = convertView.findViewById(R.id.event_title);


        // Log the poster URI
        Log.d("EventGridAdapter", "Loading image for event: " + event.getName() + " | URI: " + event.getPosterURI());


        // Use a library like Glide or Picasso to load images from URLs
        Glide.with(context)
                .load(event.getPosterURI())
                .transform(new RoundedCorners(16))
                .into(imageView);

        textView.setText(event.getName());

        return convertView;
    }

    public void setEvents(List<Event> newEvents) {
        this.events.clear();
        this.events.addAll(newEvents);
        notifyDataSetChanged(); // Notify the grid that the data has changed
    }
}

