package com.bboyairwreck.multiplayergame;

import android.app.Application;
import android.util.Log;

import com.firebase.client.Firebase;

/**
 * Created by eric on 2/27/16.
 */
public class QwicklyApplication extends Application {
    private final String TAG = QwicklyApplication.class.getSimpleName();
    public static QwicklyApplication instance;    // singleton
    private String username = "unknown";
    private Firebase qFirebase;
    private Firebase gameFirebase;

    public QwicklyApplication() {
        if (instance == null) {
            instance = this;
        } else {
            Log.e(TAG, "There is an error. You tried to create more than 1 " + TAG);
        }
    }

    public static QwicklyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Firebase.setAndroidContext(this);
        qFirebase = new Firebase(Constants.FIREBASE_URL);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        if (gameFirebase != null) {
            // TODO remove thyself from any games
            gameFirebase = null;
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public Firebase getQFirebase() {
        return qFirebase;
    }

    public void setGameFirebase(String gameID) {
        this.gameFirebase = this.qFirebase.child("games").child(gameID);
    }

    public Firebase getGameFirebase() {
        return this.gameFirebase;
    }
}
