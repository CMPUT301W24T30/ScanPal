package com.example.scanpal.Callbacks;

/**
 * An interface defining callbacks for image deletion operations.
 */
public interface ImagesDeleteCallback {

        /**
         * Called when the image deletion operation is successful.
         */
        void onSuccess();

        /**
         * Called when an error occurs during the image deletion operation.
         *
         * @param e The exception indicating the error encountered.
         */
        void onError(Exception e);
}
