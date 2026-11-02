package com.example.expensetracker.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.expensetracker.R;
import com.example.expensetracker.activities.LoginActivity;
import com.example.expensetracker.database.DatabaseHelper;
import com.example.expensetracker.firebase.FirebaseAuthManager;
import com.example.expensetracker.firebase.FirestoreManager;
import com.example.expensetracker.models.Transaction;
import com.example.expensetracker.models.User;
import com.example.expensetracker.utils.CsvExportUtils;
import com.example.expensetracker.utils.CurrencyManager;
import com.example.expensetracker.utils.DateUtils;
import com.example.expensetracker.utils.PdfExportUtils;
import com.example.expensetracker.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * User Profile & Settings fragment handling theme, currency preferences, data export, and cloud sync.
 */
public class ProfileFragment extends Fragment {

    private TextView tvProfileAvatarInitial, tvProfileName, tvProfileEmail, tvProfileMemberSince;
    private RelativeLayout rowCurrencyPreference, rowThemeSetting, rowExportPdf, rowExportCsv, rowSyncCloud, rowAboutApp;
    private TextView tvCurrentCurrencySetting, tvCurrentThemeSetting;
    private ProgressBar pbSync;
    private MaterialButton btnLogout;

    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        sessionManager = SessionManager.getInstance(requireContext());
        dbHelper = DatabaseHelper.getInstance(requireContext());
        currentUserId = sessionManager.getUserId();

        initViews(view);
        loadUserProfile();
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        tvProfileAvatarInitial = view.findViewById(R.id.tvProfileAvatarInitial);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvProfileMemberSince = view.findViewById(R.id.tvProfileMemberSince);

        rowCurrencyPreference = view.findViewById(R.id.rowCurrencyPreference);
        rowThemeSetting = view.findViewById(R.id.rowThemeSetting);
        rowExportPdf = view.findViewById(R.id.rowExportPdf);
        rowExportCsv = view.findViewById(R.id.rowExportCsv);
        rowSyncCloud = view.findViewById(R.id.rowSyncCloud);
        rowAboutApp = view.findViewById(R.id.rowAboutApp);

        tvCurrentCurrencySetting = view.findViewById(R.id.tvCurrentCurrencySetting);
        tvCurrentThemeSetting = view.findViewById(R.id.tvCurrentThemeSetting);
        pbSync = view.findViewById(R.id.pbSync);
        btnLogout = view.findViewById(R.id.btnLogout);
    }

    private void loadUserProfile() {
        String name = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();

        tvProfileName.setText(name);
        tvProfileEmail.setText(email);

        if (name != null && !name.trim().isEmpty()) {
            tvProfileAvatarInitial.setText(String.valueOf(name.trim().toUpperCase().charAt(0)));
        } else {
            tvProfileAvatarInitial.setText("U");
        }

        User u = dbHelper.getUser(currentUserId);
        if (u != null && u.getCreatedAt() > 0) {
            String dateFormatted = DateUtils.formatToDisplay(DateUtils.getCurrentDate());
            tvProfileMemberSince.setText("Active Member • " + dateFormatted);
        } else {
            tvProfileMemberSince.setText("Active Member");
        }

        // Display current currency
        String curr = sessionManager.getCurrency();
        tvCurrentCurrencySetting.setText(curr + " (" + CurrencyManager.getSymbol(curr) + ")");

        // Display current theme
        int themeMode = sessionManager.getThemeMode();
        if (themeMode == AppCompatDelegate.MODE_NIGHT_YES) {
            tvCurrentThemeSetting.setText("Dark Mode");
        } else if (themeMode == AppCompatDelegate.MODE_NIGHT_NO) {
            tvCurrentThemeSetting.setText("Light Mode");
        } else {
            tvCurrentThemeSetting.setText("System Default");
        }
    }

    private void setupListeners() {
        rowCurrencyPreference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCurrencyDialog();
            }
        });

        rowThemeSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showThemeDialog();
            }
        });

        rowExportPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportPdfStatement();
            }
        });

        rowExportCsv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportCsvTransactions();
            }
        });

        rowSyncCloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performCloudSync();
            }
        });

        rowAboutApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAboutDialog();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmLogout();
            }
        });
    }

    private void showCurrencyDialog() {
        final String[] currencies = CurrencyManager.SUPPORTED_CURRENCIES;
        final String[] codes = CurrencyManager.CURRENCY_CODES;

        String currentCode = sessionManager.getCurrency();
        int selectedIndex = 0;
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equalsIgnoreCase(currentCode)) {
                selectedIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.currency_preference)
                .setSingleChoiceItems(currencies, selectedIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newCode = codes[which];
                        sessionManager.setCurrency(newCode);
                        dbHelper.updateUserCurrency(currentUserId, newCode);
                        tvCurrentCurrencySetting.setText(currencies[which]);
                        Toast.makeText(getContext(), "Currency updated to " + newCode, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void showThemeDialog() {
        final String[] themes = {"System Default", "Light Mode", "Dark Mode"};
        final int[] modes = {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                AppCompatDelegate.MODE_NIGHT_NO,
                AppCompatDelegate.MODE_NIGHT_YES
        };

        int currentMode = sessionManager.getThemeMode();
        int selectedIndex = 0;
        for (int i = 0; i < modes.length; i++) {
            if (modes[i] == currentMode) {
                selectedIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.theme_setting)
                .setSingleChoiceItems(themes, selectedIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int selectedMode = modes[which];
                        sessionManager.setThemeMode(selectedMode);
                        AppCompatDelegate.setDefaultNightMode(selectedMode);
                        tvCurrentThemeSetting.setText(themes[which]);
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void exportPdfStatement() {
        String currentMonth = DateUtils.getCurrentMonth();
        String monthTitle = DateUtils.getCurrentMonthTitle();
        double income = dbHelper.getTotalIncome(currentUserId, currentMonth);
        double expense = dbHelper.getTotalExpense(currentUserId, currentMonth);
        List<Transaction> transactions = dbHelper.getFilteredTransactions(currentUserId, "ALL", "ALL",
                null, null, null, "date_desc");

        PdfExportUtils.generateAndSharePdf(requireContext(), sessionManager.getUserName(),
                monthTitle, income, expense, transactions);
    }

    private void exportCsvTransactions() {
        List<Transaction> allTransactions = dbHelper.getAllTransactions(currentUserId);
        CsvExportUtils.exportTransactionsToCsv(requireContext(), allTransactions);
    }

    private void performCloudSync() {
        pbSync.setVisibility(View.VISIBLE);
        rowSyncCloud.setEnabled(false);

        FirestoreManager firestore = FirestoreManager.getInstance(requireContext());
        firestore.pushLocalToCloud(requireContext(), currentUserId, new FirestoreManager.SyncCallback() {
            @Override
            public void onSyncComplete(int count) {
                if (getContext() != null) {
                    pbSync.setVisibility(View.GONE);
                    rowSyncCloud.setEnabled(true);
                    Toast.makeText(getContext(), "Cloud sync complete (" + count + " records processed)", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSyncFailed(String message) {
                if (getContext() != null) {
                    pbSync.setVisibility(View.GONE);
                    rowSyncCloud.setEnabled(true);
                    Toast.makeText(getContext(), "Sync notice: " + message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.app_full_name)
                .setMessage("Smart Expense & Budget Management App\n\n"
                        + "Version: 1.0.0\n"
                        + "Architecture: Clean MVC (Activities, Fragments, Adapters, SQLite, Firebase Firestore)\n"
                        + "Technologies: Java, XML, SQLite, Firebase Auth, Cloud Firestore, MPAndroidChart\n\n"
                        + "Developed for Final-Year B.Tech Computer Science Engineering Project & Professional Android Developer Portfolio.")
                .setPositiveButton("Close", null)
                .show();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirmation)
                .setPositiveButton(R.string.logout, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuthManager.getInstance(requireContext()).signOut(requireContext());
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
