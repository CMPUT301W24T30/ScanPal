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
import com.example.scanpal.Models.ImageData;
import com.example.scanpal.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ImageGridAdapter extends BaseAdapter {
    protected Context context;
    protected List<ImageData> images;

    /**
     * Constructs an EventGridAdapter with the specified context and event list.
     *
     * @param context The current context. Used to inflate layout files and access resources.
     */
    public ImageGridAdapter(Context context) {
        this.context = context;
    }

    /**
     * Returns the number of events in the dataset represented by this Adapter.
     *
     * @return Count of events.
     */
    @Override
    public int getCount() {
        return images.size();
    }

    /**
     * Gets the data item associated with the specified position in the data set.
     *
     * @param position Position of the item within the adapter's data set whose data we want.
     * @return The Event object at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return images.get(position);
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

        ImageData image = images.get(position);

        ImageView imageView = convertView.findViewById(R.id.event_image);
        TextView textView = convertView.findViewById(R.id.event_title);
        MaterialCardView cardView = convertView.findViewById(R.id.cardview);

        // Log the poster URI
        Log.d("EventGridAdapter", "Loading image for event: " + image.getFileName() + " | URI: " + image.getImageURL());

        Glide.with(context)
                .load(image.getImageURL())
                .transform(new RoundedCorners(16))
                .into(imageView);

        textView.setText(image.getFileName());

        cardView.setStrokeColor(Color.parseColor("#0D6EFD"));

        return convertView;
    }

    /**
     * Updates the events list of the adapter and notifies the GridView to refresh the data set.
     *
     * @param newImages The new list of events to replace the old one.
     */
    public void setImages(List<ImageData> newImages) {
        this.images.clear();
        this.images.addAll(newImages);
        notifyDataSetChanged();
    }
}
