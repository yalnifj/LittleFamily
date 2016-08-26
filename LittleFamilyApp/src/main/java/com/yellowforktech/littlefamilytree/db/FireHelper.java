package com.yellowforktech.littlefamilytree.db;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Parents on 8/26/2016.
 */
public class FireHelper implements FirebaseAuth.AuthStateListener {
    private static FireHelper instance;
    private boolean authenticated;
    private FirebaseAuth mAuth;

    public static FireHelper getInstance() {
        if (instance==null) {
            instance = new FireHelper();
        }
        return instance;
    }

    private FireHelper() {
        authenticated = false;
        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(this);
    }

    public void authenticate() {
        mAuth.signInWithEmailAndPassword("service@yellowforktech.com", "I <3 Little Family Tree").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d("FireHelper", "signInWithEmail:onComplete:" + task.isSuccessful());
                if (!task.isSuccessful()) {
                    Log.w("FireHelper", "signInWithEmail:failed", task.getException());
                } else {
                    authenticated = true;
                }
            }
        });
    }

    public void createOrUpdateUser(String username, String serviceType, boolean isPremium) {
        if (authenticated) {
            updateUser(username, serviceType, isPremium);
        } else {
            FirebaseAuth.AuthStateListener tempList = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        updateUser(username, serviceType, isPremium);
                    }
                    mAuth.removeAuthStateListener(this);
                }
            };
            mAuth.addAuthStateListener(tempList);
            authenticate();
        }
    }

    private void updateUser(String username, String serviceType, boolean isPremium) {
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
        } catch (Exception e) {
            //e.printStackTrace();
            Log.e("FireHelper", "Error updating user", e);
        }
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            // User is signed in
            Log.d("FireHelper", "onAuthStateChanged:signed_in:" + user.getUid());
            authenticated = true;
        } else {
            // User is signed out
            Log.d("FireHelper", "onAuthStateChanged:signed_out");
        }
    }
}
