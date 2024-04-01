package com.example.scanpal.Callbacks;

import com.example.scanpal.Models.User;

import java.util.List;

public interface UsersFetchCallback {
    void onSuccess(List<User> users);
    void onError(Exception e);
}