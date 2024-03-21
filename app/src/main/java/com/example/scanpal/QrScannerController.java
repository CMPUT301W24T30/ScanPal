package com.example.scanpal;

import android.util.Log;

import com.journeyapps.barcodescanner.ScanOptions;

/**
 * Handles QR Code scanning and adding events using QR codes or checking in
 */
public class QrScannerController {
    private final AttendeeController attendeeController;

    public QrScannerController(AttendeeController attendeeController) {
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

    public void customQrCode(String url) {

    }
}
