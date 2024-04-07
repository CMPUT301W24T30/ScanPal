package com.example.scanpal.Callbacks;
import java.util.List;

/**
 * An interface defining callbacks for image fetch operations.
 */
public interface ImagesFetchCallback {

        /**
         * Called when the image fetch operation is successful.
         *
         * @param images A list of URIs representing the fetched images.
         */
        void onSuccess(List<String> images);

        /**
         * Called when an error occurs during the image fetch operation.
         *
         * @param e The exception indicating the error encountered.
         */
        void onError(Exception e);
}
