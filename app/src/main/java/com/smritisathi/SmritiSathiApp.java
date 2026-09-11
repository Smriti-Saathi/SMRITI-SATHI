package com.smritisathi;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.smritisathi.data.SessionManager;

/**
 * Custom Application class for Smriti Sathi app.
 */
public class SmritiSathiApp extends Application {

    private static SmritiSathiApp instance;
    private SessionManager sessionManager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Initialize Firebase and enable native Firestore offline persistence
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
            }
            com.google.firebase.firestore.FirebaseFirestoreSettings settings =
                    new com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                            .setPersistenceEnabled(true)
                            .build();
            com.google.firebase.firestore.FirebaseFirestore.getInstance().setFirestoreSettings(settings);
        } catch (Exception e) {
            e.printStackTrace();
        }

        sessionManager = new SessionManager(this);
    }

    public static SmritiSathiApp getInstance() {
        return instance;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
