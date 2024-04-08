package com.example.scanpal.ControllersTest;

import android.graphics.Bitmap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import com.example.scanpal.Controllers.QrCodeController;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {34})
public class QrCodeControllerTest {

    @Test
    public void generate_ValidData_ReturnsBitmap() {
        // Arrange
        String testData = "Test Data";

        // Act
        Bitmap resultBitmap = QrCodeController.generate(testData);

        // Assert
        assertNotNull("Bitmap should not be null", resultBitmap);
        assertEquals("Bitmap width should be 300", 300, resultBitmap.getWidth());
        assertEquals("Bitmap height should be 300", 300, resultBitmap.getHeight());
    }

    @Test
    public void generate_EmptyString_ReturnsNull() {
        // Arrange
        String testData = ""; // Empty string is considered invalid for QR code generation

        // Act
        Bitmap resultBitmap = QrCodeController.generate(testData);

        // Assert
        assertNull("Bitmap should be null for empty string data", resultBitmap);
    }


    @Test
    public void generate_InvalidData_ReturnsNull() {
        // Arrange
        String testData = null; // Assume this is invalid data that causes a WriterException

        // Act
        Bitmap resultBitmap = QrCodeController.generate(testData);

        // Assert
        assertNull("Bitmap should be null due to invalid data", resultBitmap);
    }

    @Test
    public void bitmapToByteArray_ValidBitmap_ConvertsSuccessfully() {
        // Arrange
        Bitmap bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);

        // Act
        byte[] byteArray = QrCodeController.bitmapToByteArray(bitmap);

        // Assert
        assertNotNull("Byte array should not be null", byteArray);
        assertTrue("Byte array should have a length", byteArray.length > 0);
    }
}
