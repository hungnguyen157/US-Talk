package com.example.ustalk;

import android.app.Activity;
import android.util.Log;

import com.example.ustalk.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

public class OnlineActivity extends Activity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onPause() {
        super.onPause();
        String uid = new PreferenceManager(getApplicationContext()).getString("UID");
        if (uid != null)  db.collection("users").document(uid).update("online", false)
                .addOnFailureListener(e -> Log.e("online", e.getMessage()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        String uid = new PreferenceManager(getApplicationContext()).getString("UID");
        if (uid != null)  db.collection("users").document(uid).update("online", true)
                .addOnFailureListener(e -> Log.e("online", e.getMessage()));
    }
}
