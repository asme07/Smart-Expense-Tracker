package com.example.expensetracker.firebase;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.expensetracker.database.DatabaseHelper;
import com.example.expensetracker.models.User;
import com.example.expensetracker.utils.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * Manages Firebase Authentication operations with local SQLite fallback.
 */
public class FirebaseAuthManager {

    private static final String TAG = "FirebaseAuthManager";
    private static FirebaseAuthManager instance;
    private FirebaseAuth firebaseAuth;
    private boolean isFirebaseAvailable = false;

    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String errorMessage);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public static synchronized FirebaseAuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseAuthManager(context.getApplicationContext());
        }
        return instance;
    }

    private FirebaseAuthManager(Context context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context);
            }
            firebaseAuth = FirebaseAuth.getInstance();
            isFirebaseAvailable = (firebaseAuth != null);
        } catch (Exception e) {
            Log.w(TAG, "Firebase initialization skipped or not configured: " + e.getMessage());
            isFirebaseAvailable = false;
        }
    }

    public boolean isFirebaseAvailable() {
        return isFirebaseAvailable;
    }

    public void register(Context context, String name, String email, String password, AuthCallback callback) {
        if (!isFirebaseAvailable) {
            // Local fallback registration
            registerLocally(context, name, email, callback);
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful() && firebaseAuth.getCurrentUser() != null) {
                            FirebaseUser fbUser = firebaseAuth.getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            fbUser.updateProfile(profileUpdates);

                            User user = new User(fbUser.getUid(), name, email, "USD", System.currentTimeMillis());
                            // Save locally
                            DatabaseHelper.getInstance(context).insertOrUpdateUser(user);
                            SessionManager.getInstance(context).createLoginSession(user.getUserId(), user.getName(), user.getEmail());
                            // Sync profile to Firestore
                            FirestoreManager.getInstance(context).saveUserProfile(user, null);

                            callback.onSuccess(user);
                        } else {
                            String err = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                            callback.onError(err);
                        }
                    }
                });
    }

    public void login(Context context, String email, String password, AuthCallback callback) {
        if (!isFirebaseAvailable) {
            // Local fallback login
            loginLocally(context, email, callback);
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful() && firebaseAuth.getCurrentUser() != null) {
                            FirebaseUser fbUser = firebaseAuth.getCurrentUser();
                            String name = !TextUtils.isEmpty(fbUser.getDisplayName()) ? fbUser.getDisplayName() : "Smart User";
                            User user = new User(fbUser.getUid(), name, email, "USD", System.currentTimeMillis());

                            DatabaseHelper.getInstance(context).insertOrUpdateUser(user);
                            SessionManager.getInstance(context).createLoginSession(user.getUserId(), user.getName(), user.getEmail());

                            callback.onSuccess(user);
                        } else {
                            String err = task.getException() != null ? task.getException().getMessage() : "Invalid credentials";
                            callback.onError(err);
                        }
                    }
                });
    }

    public void resetPassword(String email, ActionCallback callback) {
        if (!isFirebaseAvailable) {
            callback.onSuccess();
            return;
        }

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            String err = task.getException() != null ? task.getException().getMessage() : "Failed to send reset email";
                            callback.onError(err);
                        }
                    }
                });
    }

    public void signOut(Context context) {
        if (isFirebaseAvailable && firebaseAuth != null) {
            firebaseAuth.signOut();
        }
        SessionManager.getInstance(context).logout();
    }

    // Local Fallback for offline testing or without active Firebase project
    public void registerLocally(Context context, String name, String email, AuthCallback callback) {
        String localId = "usr_" + Math.abs(email.hashCode());
        User user = new User(localId, name, email, "USD", System.currentTimeMillis());
        DatabaseHelper.getInstance(context).insertOrUpdateUser(user);
        SessionManager.getInstance(context).createLoginSession(user.getUserId(), user.getName(), user.getEmail());
        callback.onSuccess(user);
    }

    public void loginLocally(Context context, String email, AuthCallback callback) {
        String localId = "usr_" + Math.abs(email.hashCode());
        User existing = DatabaseHelper.getInstance(context).getUser(localId);
        if (existing == null) {
            existing = new User(localId, "Demo User", email, "USD", System.currentTimeMillis());
            DatabaseHelper.getInstance(context).insertOrUpdateUser(existing);
        }
        SessionManager.getInstance(context).createLoginSession(existing.getUserId(), existing.getName(), existing.getEmail());
        callback.onSuccess(existing);
    }
}
