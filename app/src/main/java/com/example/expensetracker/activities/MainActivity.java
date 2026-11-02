package com.example.expensetracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.expensetracker.R;
import com.example.expensetracker.fragments.AnalyticsFragment;
import com.example.expensetracker.fragments.BudgetFragment;
import com.example.expensetracker.fragments.HomeFragment;
import com.example.expensetracker.fragments.ProfileFragment;
import com.example.expensetracker.fragments.TransactionsFragment;
import com.example.expensetracker.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

/**
 * Main Activity hosting the Bottom Navigation and primary application fragments.
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddTransaction;

    private Fragment homeFragment;
    private Fragment transactionsFragment;
    private Fragment budgetFragment;
    private Fragment analyticsFragment;
    private Fragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Security check
        if (!SessionManager.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        initViews();
        setupFragments();
        setupNavigation();
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAddTransaction = findViewById(R.id.fabAddTransaction);

        fabAddTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TransactionEditorActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupFragments() {
        homeFragment = new HomeFragment();
        transactionsFragment = new TransactionsFragment();
        budgetFragment = new BudgetFragment();
        analyticsFragment = new AnalyticsFragment();
        profileFragment = new ProfileFragment();

        // Default to Home Fragment
        loadFragment(homeFragment);
    }

    private void setupNavigation() {
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    loadFragment(homeFragment);
                    fabAddTransaction.show();
                    return true;
                } else if (itemId == R.id.nav_transactions) {
                    loadFragment(transactionsFragment);
                    fabAddTransaction.show();
                    return true;
                } else if (itemId == R.id.nav_budget) {
                    loadFragment(budgetFragment);
                    fabAddTransaction.hide();
                    return true;
                } else if (itemId == R.id.nav_analytics) {
                    loadFragment(analyticsFragment);
                    fabAddTransaction.hide();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    loadFragment(profileFragment);
                    fabAddTransaction.hide();
                    return true;
                }
                return false;
            }
        });
    }

    public void navigateToTab(int menuItemId) {
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(menuItemId);
        }
    }

    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}
