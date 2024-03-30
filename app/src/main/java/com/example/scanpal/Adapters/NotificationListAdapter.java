package com.example.scanpal.Adapters;

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
import com.example.scanpal.Callbacks.EventFetchCallback;
import com.example.scanpal.Controllers.EventController;
import com.example.scanpal.Models.Announcement;
import com.example.scanpal.Models.Event;
import com.example.scanpal.R;

import java.util.List;

public class NotificationListAdapter extends BaseAdapter {
    protected Context context;
    protected List<Announcement> notifications;
    private Event eventItem;


    public NotificationListAdapter(Context context, List<Announcement> values) {
        //super(context, R.layout.notif_list_layout, values);
        this.context = context;
        this.notifications = values;
    }

    @Override
    public int getCount() {
        return notifications.size();
    }

    @Override
    public Object getItem(int position) {
        return notifications.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.notif_list_layout, parent, false);
        }

        Announcement announcement = notifications.get(position);

        ImageView imageView = convertView.findViewById(R.id.notif_image);
        TextView textView = convertView.findViewById(R.id.Notification_Header);
        TextView textView1 = convertView.findViewById(R.id.Notification_message);
        TextView textView2 = convertView.findViewById(R.id.Notification_Time);

        EventController eventController = new EventController();
        //Event event = new Event(null,"","");
        //Event eventItem = ;
        eventController.getEventById(announcement.getEventID(), new EventFetchCallback() {
            @Override
            public void onSuccess(Event event) {
                eventItem = event;
                Log.d("NotifListAdapter", "Loading image for event: " + eventItem.getName() + " | URI: " + eventItem.getPosterURI());
                Glide.with(context)
                        .load(eventItem.getPosterURI())
                        .transform(new RoundedCorners(16))
                        .into(imageView);

                textView.setText(eventItem.getName());
                textView1.setText(announcement.getMessage());
                textView2.setText(announcement.getTimeStamp());

            }

            @Override
            public void onError(Exception e) {

            }
        });


        return convertView;
    }

    public void setNotifications(List<Announcement> newAnnouncements) {
        this.notifications.clear();
        this.notifications.addAll(newAnnouncements);
        notifyDataSetChanged();
    }
}
