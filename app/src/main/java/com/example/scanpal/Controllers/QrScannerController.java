package com.example.scanpal.Controllers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.scanpal.Callbacks.AttendeeFetchCallback;
import com.example.scanpal.Callbacks.AttendeeUpdateCallback;
import com.example.scanpal.Callbacks.EventFetchCallback;
import com.example.scanpal.Callbacks.QrScanResultCallback;
import com.example.scanpal.Models.Attendee;
import com.example.scanpal.Models.Capture;
import com.example.scanpal.Models.Event;
import com.journeyapps.barcodescanner.ScanOptions;

/**
 * Handles QR Code scanning and adding events using QR codes or checking in
 */
public class QrScannerController {
    private final AttendeeController attendeeController;
    private final EventController eventController = new EventController();
    private final Context context;
    protected String eventID = null;

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
     * Handles the logic for checking in/adding event functionality after scanning a
     * valid QR code
     *
     * @param qrId     A string of the eventID contained inside the QR code after
     *                 scanning
     * @param username Username of the attendee who scanned the QR code
     */
    public void handleResult(String qrId, String username, QrScanResultCallback callback) {
        if (qrId.startsWith("C")) {
            String eventID = qrId.substring(1);

            eventController.getEventById(eventID, new EventFetchCallback() {
                @Override
                public void onSuccess(Event event) {
                    Log.d("HANDLE_RESULT", "Handling event check-in or addition based on QR.");
                    String attendeeId = username + eventID;
                    attendeeController.fetchAttendee(attendeeId, new AttendeeFetchCallback() {
                        @Override
                        public void onSuccess(Attendee attendee) {
                            attendee.setCheckedIn(true);
                            attendee.setCheckinCount(attendee.getCheckinCount() + 1L);

                            attendeeController.updateAttendee(attendee, new AttendeeUpdateCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(context, "Checked-in! 🎉", Toast.LENGTH_SHORT).show();
                                    Log.d("CHECKED IN!", "Attendee checked-in successfully!");
                                    callback.onResult(eventID);
                                }

                                @Override
                                public void onError(Exception e) {
                                    Toast.makeText(context, "Check-in Failed 😔", Toast.LENGTH_SHORT).show();
                                    Log.d("NOT CHECKED IN!", "Check-in failed", e);
                                    callback.onResult(eventID);
                                }
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(context, "Check-in Failed. Make sure to RSVP before joining an event.",
                                    Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", "Error fetching attendee: " + e.getMessage(), e);
                            callback.onResult(eventID);
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(context, "Check-in Failed. Error Fetching Event.", Toast.LENGTH_SHORT).show();
                    Log.e("ERROR", "Error fetching event details: " + e.getMessage(), e);
                    callback.onError("Error fetching event details.");
                }
            });

        } else if (qrId.startsWith("E")) {
            callback.onResult(qrId.substring(1));
        } else {
            callback.onError("Invalid QR code.");
        }
    }
}
