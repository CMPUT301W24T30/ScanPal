package com.example.scanpal;

import com.example.scanpal.Models.Administrator;
import org.junit.Test;
import static org.junit.Assert.*;

public class AdministratorTest {

    @Test
    public void constructor_correctlySetsAdministratorFlag() {
        Administrator admin = new Administrator("adminUsername", "AdminFirstName", "AdminLastName", "adminDeviceToken");

        assertTrue("The administrator flag should be true for Administrator instances", admin.isAdministrator());

        assertEquals("Username should match the constructor argument", "adminUsername", admin.getUsername());
        assertEquals("First name should match the constructor argument", "AdminFirstName", admin.getFirstName());
        assertEquals("Last name should match the constructor argument", "AdminLastName", admin.getLastName());
        assertEquals("Device token should match the constructor argument", "adminDeviceToken", admin.getDeviceToken());
    }
}
