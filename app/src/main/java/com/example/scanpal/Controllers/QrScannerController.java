package com.example.scanpal.Controllers;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.navigation.NavController;

import com.example.scanpal.Callbacks.AttendeeFetchCallback;
import com.example.scanpal.Callbacks.AttendeeUpdateCallback;
import com.example.scanpal.Callbacks.EventFetchCallback;
import com.example.scanpal.Callbacks.UserFetchCallback;
import com.example.scanpal.Models.Attendee;
import com.example.scanpal.Models.Capture;
import com.example.scanpal.Models.Event;
import com.example.scanpal.Models.User;
import com.example.scanpal.R;
import com.journeyapps.barcodescanner.ScanOptions;

/**
 * Handles QR Code scanning and adding events using QR codes or checking in
 */
public class QrScannerController {
    private final AttendeeController attendeeController;
    private final EventController eventController = new EventController();
    private final Context context;

    public QrScannerController(Context context, AttendeeController attendeeController) {
        this.context = context;
        this.attendeeController = attendeeController;
    }

    /**
     * Sets the options need to run a QR Code Scan
     *
     * @return preliminary options to allows the launch of the scan
     */
    public static ScanOptions getOptions() {
        ScanOptions options = new ScanOptions();
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(Capture.class);
        options.setPrompt("Place QR Code inside the viewfinder");
        return options;
    }

    /**
     * Handles the logic for checking in/adding event functionality after scanning a valid QR code
     *
     * @param qrId     A string of the eventID contained inside the QR code after scanning
     * @param username Username of the attendee who scanned the QR code
     */
    public void handleResult(String qrId, String username) {
        if (qrId.startsWith("C")) {
            String eventId = qrId.substring(1);

            eventController.getEventById(eventId, new EventFetchCallback() {
                @Override
                public void onSuccess(Event event) {

                    if (event.isTrackLocation()) {
                        // Proceed to get user location and update attendee
                        updateUserLocationAndAttendee(eventId, username);
                    } else {
                        // Handle case where tracking is not required
                        Log.d("HANDLE_RESULT", "Location tracking not required for this event.");
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e("HANDLE_RESULT", "Error fetching event details: " + e.getMessage());
                }
            });

            String attendeeId = username + eventId;
            Log.d("ATTENDEE", attendeeId);

            attendeeController.fetchAttendee(attendeeId, new AttendeeFetchCallback() {
                @Override
                public void onSuccess(Attendee attendee) {
                    attendee.setCheckedIn(true);
                    attendee.setCheckinCount(attendee.getCheckinCount() + 1L);

                    Log.wtf("CHECKED IN!", " crash not here: in callback " + attendee.getUser().getUsername());

                    attendeeController.updateAttendee(attendee, new AttendeeUpdateCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(context.getApplicationContext(), "Checked-in! 🎉", Toast.LENGTH_SHORT).show();
                            Log.wtf("CHECKED IN!", "Attendee checked-in successfully!");
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(context.getApplicationContext(), "Checked-in Failed 😔", Toast.LENGTH_SHORT).show();
                            Log.wtf("NOT CHECKED IN!", "check-in failed");
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    System.err.println("Error fetching attendee: " + e.getMessage());
                }
            });
        } else if (qrId.startsWith("E")) {
            // Navigate user to event with id and display it
            String eventId = qrId.substring(1);
            eventController.getEventById(eventId, new EventFetchCallback() {
                @Override
                public void onSuccess(Event event) {
                    NavController navController = new NavController(context);
                    Bundle bundle = new Bundle();
                    bundle.putString("event_id", event.getId());
                    navController.navigate(R.id.eventDetailsPage, bundle);
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(context, "Error Retrieving Event", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void updateUserLocationAndAttendee(String eventId, String username) {
        UserController userController = new UserController(context);

        userController.getUser(username, new UserFetchCallback() {
            @Override
            public void onSuccess(User user) {
                String userLocation = user.getLocation();
                String attendeeId = username + eventId;

                attendeeController.fetchAttendee(attendeeId, new AttendeeFetchCallback() {
                    @Override
                    public void onSuccess(Attendee attendee) {

                        attendee.setLocation(userLocation);
                        attendee.setCheckedIn(true);
                        attendeeController.updateAttendee(attendee, new AttendeeUpdateCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d("QrScannerController", "Attendee location updated successfully.");
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("QrScannerController", "Error updating attendee location: " + e.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("QrScannerController", "Error fetching attendee: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("QrScannerController", "Error fetching user for location update: " + e.getMessage());
            }
        });
    }


}
