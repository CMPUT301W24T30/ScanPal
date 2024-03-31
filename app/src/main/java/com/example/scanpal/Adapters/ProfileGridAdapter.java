package com.example.scanpal.Adapters;

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
import com.example.scanpal.Models.User;
import com.example.scanpal.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class ProfileGridAdapter extends BaseAdapter {
    protected Context context;
    protected List<User> users = new ArrayList<>();

    /**
     * Constructs an EventGridAdapter with the specified context and event list.
     *
     * @param context The current context. Used to inflate layout files and access resources.
     */
    public ProfileGridAdapter(Context context) {
        this.context = context;
    }

    /**
     * Returns the number of events in the dataset represented by this Adapter.
     *
     * @return Count of events.
     */
    @Override
    public int getCount() {
        return users.size();
    }

    /**
     * Gets the data item associated with the specified position in the data set.
     *
     * @param position Position of the item within the adapter's data set whose data we want.
     * @return The Event object at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return users.get(position);
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

        User user = users.get(position);

        ImageView imageView = convertView.findViewById(R.id.event_image);
        TextView textView = convertView.findViewById(R.id.event_title);
        MaterialCardView cardView = convertView.findViewById(R.id.cardview);

        // Log the poster URI
        Log.d("EventGridAdapter", "Loading image for user: " + user.getUsername() + " | Photo: " + user.getPhoto());

        Glide.with(context)
                .load(user.getPhoto())
                .transform(new RoundedCorners(16))
                .into(imageView);

        textView.setText(user.getUsername());

        cardView.setStrokeColor(Color.parseColor("#0D6EFD"));

        return convertView;
    }

    /**
     * Updates the events list of the adapter and notifies the GridView to refresh the data set.
     *
     * @param newUsers The new list of users to replace the old one.
     */
    public void setUsers(List<User> newUsers) {
        this.users.clear();
        this.users.addAll(newUsers);
        notifyDataSetChanged();
    }
}
