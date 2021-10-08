package com.ethicalsight.reward;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;


public class NotificationService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }
}
