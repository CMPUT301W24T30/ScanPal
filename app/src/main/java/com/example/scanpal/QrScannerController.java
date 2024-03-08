package com.example.scanpal;

import com.journeyapps.barcodescanner.ScanOptions;

/**
 * Handles QR Code scanning and adding events using Qr codes or checking in
 */

public class QrScannerController {

    /**
     * Sets the options need to run a Qr Code Scan
     *
     * @return preliminary options to allows the launch of the scan
     */
    public static ScanOptions getOptions() {
        ScanOptions options = new ScanOptions();
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(Capture.class);
        options.setPrompt("Place Qr Code inside the viewfinder");
        return options;
    }

    /**
     * Handles the logic for checkin/adding event functionality after scanning a valid qr code
     *
     * @param qr_id A string of the id contained inside the qr code after scanned
     */
    public static void handleResult(String qr_id) {

            if (qr_id.charAt(0) == 'C') {  // for check in
                qr_id = qr_id.substring(1);  // get rid of starter code

                // Add user as checked in to event

            } else if (qr_id.charAt(0) == 'E') {  // for joining event
                qr_id = qr_id.substring(1);  // get rid of starter code

                // Add user as
            }

    }
}
