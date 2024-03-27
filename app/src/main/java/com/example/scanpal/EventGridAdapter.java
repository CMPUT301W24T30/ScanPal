package com.example.scanpal;

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
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class EventGridAdapter extends BaseAdapter {
    protected Context context;
    protected List<Event> events;

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

    public void setEvents(List<Event> newEvents) {
        this.events.clear();
        this.events.addAll(newEvents);
        notifyDataSetChanged(); // Notify the grid that the data has changed
    }
}
