package com.yellowforktech.littlefamilytree.db;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if (!authenticated) {
            mAuth.signInWithEmailAndPassword("service@yellowforktech.com", "I <3 Little Family Tree").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(Task<AuthResult> task) {
                    Log.d("FireHelper", "signInWithEmail:onComplete:" + task.isSuccessful());
                    if (!task.isSuccessful()) {
                        Log.w("FireHelper", "signInWithEmail:failed", task.getException());
                    } else {
                        authenticated = true;
                    }
                }
            });
        }
    }

    public void createOrUpdateUser(final String usern, final String serviceType, final boolean isPremium) {
        final String username = safeUsername(usern);
        if (authenticated) {
            updateUser(username, serviceType, isPremium);
        } else {
            FirebaseAuth.AuthStateListener tempList = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
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

    private void updateUser(final String usern, final String serviceType, final boolean isPremium) {
        final String username = safeUsername(usern);
        try {
            final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            database.child("users").child(serviceType).child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot!=null && dataSnapshot.exists()) {
                        List<String> platforms = (List<String>) dataSnapshot.child("platforms").getValue();
                        if (!platforms.contains("android")) {
                            platforms.add("android");
                            database.child("users/"+serviceType+"/"+username+"/platforms").setValue(platforms);
                        }
                        if (isPremium) {
                            database.child("users/"+serviceType+"/"+username+"/androidPremium").setValue(isPremium);
                        }
                    } else {
                        Map<String, Object> user = new HashMap<>();
                        user.put("username", username);
                        List<String> platforms = new ArrayList<>(1);
                        platforms.add("android");
                        user.put("platforms", platforms);
                        user.put("androidPremium", isPremium);
                        database.child("users").child(serviceType).child(username).setValue(user);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } catch (Exception e) {
            //e.printStackTrace();
            Log.e("FireHelper", "Error updating user", e);
        }
    }

    public void userIsPremium(final String usern, final String serviceType, final PremiumListener listener) {
        final String username = safeUsername(usern);
        if (authenticated) {
            isPremium(username, serviceType, listener);
        } else {
            FirebaseAuth.AuthStateListener tempList = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        isPremium(username, serviceType, listener);
                    }
                    mAuth.removeAuthStateListener(this);
                }
            };
            mAuth.addAuthStateListener(tempList);
            authenticate();
        }
    }

    private void isPremium(String usern, String serviceType, final PremiumListener listener) {
        final String username = safeUsername(usern);
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        database.child("users").child(serviceType).child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot!=null && dataSnapshot.exists()) {
                    Boolean isPremium = (Boolean) dataSnapshot.child("androidPremium").getValue();
                    if (isPremium!=null) {

                        listener.results(isPremium);
                        return;
                    }
                }
                listener.results(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.results(false);
            }
        });
    }

    public void validateCode(final String usern, final String code, final ValidateListener listener) {
       final String username = safeUsername(usern);
        if (authenticated) {
            validateCodeImpl(username, code, listener);
        } else {
            FirebaseAuth.AuthStateListener tempList = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        validateCodeImpl(username, code, listener);
                    }
                    mAuth.removeAuthStateListener(this);
                }
            };
            mAuth.addAuthStateListener(tempList);
            authenticate();
        }
    }

    private void validateCodeImpl(final String usern, final String code, final ValidateListener listener) {
        final String username = safeUsername(usern);
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        database.child("codes").child(code).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot!=null && dataSnapshot.exists()) {
                    Boolean redeemed = (Boolean) dataSnapshot.child("redeemed").getValue();
                    if (redeemed!=null && redeemed==true) {
                        String oldUser = (String) dataSnapshot.child("user").getValue();
                        if (oldUser!=null && oldUser.equals(username)) {
                            listener.results(true, "Code already validated for this user.");
                            return;
                        }
                    } else {
                        database.child("codes/"+code+"/redeemed").setValue(true);
                        database.child("codes/"+code+"/user").setValue(username);
                        listener.results(true, "Code validated.");
                        return;
                    }
                }
                listener.results(false, "Invalid code.");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.results(false, "Validation request cancelled.");
            }
        });
    }

    public String safeUsername(String username) {
        String safe = username.replaceAll("\\W", "_");
        return safe;
    }

    @Override
    public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
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

    public interface PremiumListener {
        public void results(boolean premium);
    }

    public interface ValidateListener {
        public void results(boolean valid, String status);
    }
}
