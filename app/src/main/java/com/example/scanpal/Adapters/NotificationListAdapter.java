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


/**
 * Adapter for displaying Notification data in a list format. This adapter binds some of the relateted
 * event's data to views containing the event poster and message
 */
public class NotificationListAdapter extends BaseAdapter {
    protected Context context;
    protected List<Announcement> notifications;
    private Event eventItem;


    /**
     * Constructs an NotificationListAdapter with the specified context and notifications list.
     *
     * @param context The current context. Used to inflate layout files and access resources.
     */
    public NotificationListAdapter(Context context, List<Announcement> values) {
        this.context = context;
        this.notifications = values;
    }

    /**
     * Gets the number of Announcements that have been sent out from this event
     * @return The number of Notifications
     *
     */
    @Override
    public int getCount() {
        return notifications.size();
    }

    /**
     * Gets the data item associated with the specified position in the data set.
     *
     * @param position Position of the item within the adapter's data set whose data we want.
     * @return The Notification object at the specified position.
     */
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

    /**
     * Updates the notifications list of the adapter and notifies the Listview to refresh the data set.
     *
     * @param newAnnouncements The new list of events to replace the old one.
     */
    public void setNotifications(List<Announcement> newAnnouncements) {
        this.notifications.clear();
        this.notifications.addAll(newAnnouncements);
        notifyDataSetChanged();
    }
}
