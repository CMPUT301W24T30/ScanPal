package com.example.scanpal;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanOptions;

/**
 * Handles QR Code scanning and adding events using QR codes or checking in
 */
public class QrScannerController {
    private Context context;
    private final AttendeeController attendeeController;
    private final EventController eventController = new EventController();

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
                    attendeeController.updateAttendee(attendee, new AttendeeUpdateCallback() {
                        @Override
                        public void onSuccess() {
                            Log.wtf("CHECKED IN!", "Attendee checked-in successfully!");
                        }

                        @Override
                        public void onError(Exception e) {
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
            // TODO: handle event check-in?
        }
    }

    public void updateUserLocationAndAttendee(String eventId, String username) {
        UserController userController = new UserController(FirebaseFirestore.getInstance(), context);

        userController.getUser(username, new UserFetchCallback() {
            @Override
            public void onSuccess(User user) {
                String userLocation = user.getLocation();
                String attendeeId = username + eventId;
                Log.d("QrScannerController", "Fetching attendee with ID: " + attendeeId);

                attendeeController.fetchAttendee(attendeeId, new AttendeeFetchCallback() {
                    @Override
                    public void onSuccess(Attendee attendee) {
                        Log.d("QrScannerController", "Fetching attendee with ID: " + attendee.getUser());
                        Log.d("QrScannerController", "Fetching attendee with ID: " + attendee.getLocation());
                        Log.d("QrScannerController", "Fetching attendee with details: " + attendee.isCheckedIn());


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
