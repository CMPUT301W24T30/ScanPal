package com.example.scanpal.Controllers;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.scanpal.Models.Event;
import com.example.scanpal.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * Adapter for displaying event data in a grid format. This adapter binds event data to views
 * represented by grid items in a GridView.
 */
public class EventGridAdapter extends BaseAdapter {
    protected Context context;
    protected List<Event> events;

    /**
     * Constructs an EventGridAdapter with the specified context and event list.
     *
     * @param context The current context. Used to inflate layout files and access resources.
     * @param events  A list of event models to be displayed in the grid.
     */
    public EventGridAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
    }

    /**
     * Returns the number of events in the dataset represented by this Adapter.
     *
     * @return Count of events.
     */
    @Override
    public int getCount() {
        return events.size();
    }

    /**
     * Gets the data item associated with the specified position in the data set.
     *
     * @param position Position of the item within the adapter's data set whose data we want.
     * @return The Event object at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return events.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set.
     * @return The id of the item at the specified position.
     */
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
        MaterialCardView cardView = convertView.findViewById(R.id.cardview);

        // Log the poster URI
        Log.d("EventGridAdapter", "Loading image for event: " + event.getName() + " | URI: " + event.getPosterURI());

        Glide.with(context)
                .load(event.getPosterURI())
                .transform(new RoundedCorners(16))
                .into(imageView);

        textView.setText(event.getName());

        int strokeColor = event.isUserSignedUp() ? Color.parseColor("#4CAF50") : Color.parseColor("#0D6EFD");
        cardView.setStrokeColor(strokeColor);

        return convertView;
    }

    /**
     * Updates the events list of the adapter and notifies the GridView to refresh the data set.
     *
     * @param newEvents The new list of events to replace the old one.
     */
    public void setEvents(List<Event> newEvents) {
        this.events.clear();
        this.events.addAll(newEvents);
        notifyDataSetChanged();
    }
}
