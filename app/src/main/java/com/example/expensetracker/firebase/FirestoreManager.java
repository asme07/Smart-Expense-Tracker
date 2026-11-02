package com.example.expensetracker.firebase;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.expensetracker.database.DatabaseHelper;
import com.example.expensetracker.models.Budget;
import com.example.expensetracker.models.Transaction;
import com.example.expensetracker.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

/**
 * Handles cloud synchronization with Firebase Cloud Firestore.
 */
public class FirestoreManager {

    private static final String TAG = "FirestoreManager";
    private static final String COLLECTION_USERS = "users";
    private static final String SUB_TRANSACTIONS = "transactions";
    private static final String SUB_BUDGETS = "budgets";

    private static FirestoreManager instance;
    private FirebaseFirestore firestore;
    private boolean isAvailable = false;

    public interface SyncCallback {
        void onSyncComplete(int count);
        void onSyncFailed(String message);
    }

    public static synchronized FirestoreManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirestoreManager(context.getApplicationContext());
        }
        return instance;
    }

    private FirestoreManager(Context context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context);
            }
            firestore = FirebaseFirestore.getInstance();
            isAvailable = (firestore != null);
        } catch (Exception e) {
            Log.w(TAG, "Firestore not initialized or offline: " + e.getMessage());
            isAvailable = false;
        }
    }

    public void saveUserProfile(User user, final SyncCallback callback) {
        if (!isAvailable || user == null) return;
        firestore.collection(COLLECTION_USERS)
                .document(user.getUserId())
                .set(user.toMap())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (callback != null) {
                            if (task.isSuccessful()) {
                                callback.onSyncComplete(1);
                            } else {
                                callback.onSyncFailed(task.getException() != null ? task.getException().getMessage() : "Sync error");
                            }
                        }
                    }
                });
    }

    public void uploadTransaction(Transaction t) {
        if (!isAvailable || t == null) return;
        firestore.collection(COLLECTION_USERS)
                .document(t.getUserId())
                .collection(SUB_TRANSACTIONS)
                .document(t.getTransactionId())
                .set(t.toMap());
    }

    public void deleteTransaction(String userId, String transactionId) {
        if (!isAvailable) return;
        firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUB_TRANSACTIONS)
                .document(transactionId)
                .delete();
    }

    public void uploadBudget(Budget b) {
        if (!isAvailable || b == null) return;
        firestore.collection(COLLECTION_USERS)
                .document(b.getUserId())
                .collection(SUB_BUDGETS)
                .document(b.getBudgetId())
                .set(b.toMap());
    }

    public void deleteBudget(String userId, String budgetId) {
        if (!isAvailable) return;
        firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUB_BUDGETS)
                .document(budgetId)
                .delete();
    }

    public void syncAllFromCloud(final Context context, final String userId, final SyncCallback callback) {
        if (!isAvailable) {
            if (callback != null) callback.onSyncComplete(0);
            return;
        }

        // Pull transactions from Firestore and save into SQLite
        firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUB_TRANSACTIONS)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            int count = 0;
                            DatabaseHelper db = DatabaseHelper.getInstance(context);
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                Transaction t = doc.toObject(Transaction.class);
                                if (t != null) {
                                    db.insertTransaction(t);
                                    count++;
                                }
                            }

                            // Sync Budgets
                            syncBudgetsFromCloud(context, userId, count, callback);
                        } else {
                            if (callback != null) {
                                callback.onSyncFailed(task.getException() != null ? task.getException().getMessage() : "Sync failed");
                            }
                        }
                    }
                });
    }

    private void syncBudgetsFromCloud(final Context context, String userId, final int initialCount, final SyncCallback callback) {
        firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUB_BUDGETS)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        int total = initialCount;
                        if (task.isSuccessful() && task.getResult() != null) {
                            DatabaseHelper db = DatabaseHelper.getInstance(context);
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                Budget b = doc.toObject(Budget.class);
                                if (b != null) {
                                    db.insertOrUpdateBudget(b);
                                    total++;
                                }
                            }
                        }
                        if (callback != null) callback.onSyncComplete(total);
                    }
                });
    }

    public void pushLocalToCloud(Context context, String userId, final SyncCallback callback) {
        if (!isAvailable) {
            if (callback != null) callback.onSyncComplete(0);
            return;
        }

        DatabaseHelper db = DatabaseHelper.getInstance(context);
        List<Transaction> transactions = db.getAllTransactions(userId);
        for (Transaction t : transactions) {
            uploadTransaction(t);
        }

        List<Budget> budgets = db.getBudgets(userId, com.example.expensetracker.utils.DateUtils.getCurrentMonth());
        for (Budget b : budgets) {
            uploadBudget(b);
        }

        if (callback != null) {
            callback.onSyncComplete(transactions.size() + budgets.size());
        }
    }
}
