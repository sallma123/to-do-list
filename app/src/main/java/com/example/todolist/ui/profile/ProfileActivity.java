package com.example.todolist.ui.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.todolist.R;
import com.example.todolist.ui.auth.ChangePasswordActivity;
import com.example.todolist.ui.auth.LoginActivity;
import com.example.todolist.ui.calendar.CalendarActivity;
import com.example.todolist.ui.main.MainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {

    private TextView emailText, nameText, totalTasks, completionRate;
    private Button changePasswordButton;

    private int userId;
    private ProfileViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialisation des vues
        emailText = findViewById(R.id.emailText);
        nameText = findViewById(R.id.nameText);
        totalTasks = findViewById(R.id.totalTasks);
        completionRate = findViewById(R.id.completionRate);
        changePasswordButton = findViewById(R.id.changePasswordButton);

        // Récupérer l'ID utilisateur depuis l'Intent
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            finish();
            return;
        }

        // Initialiser ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Observer les données utilisateur
        viewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                emailText.setText("Email: " + user.getEmail());
                nameText.setText("Name: " + (user.getName() != null ? user.getName() : "(not set)"));
            }
        });

        // Observer les statistiques
        viewModel.getStatsLiveData().observe(this, stats -> {
            totalTasks.setText(String.valueOf(stats.total));
            completionRate.setText(stats.completionRate + "%");
        });

        // Charger les données
        viewModel.loadUser(userId);
        viewModel.loadStats(userId);

        // Bouton pour changer le mot de passe
        changePasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });

        // Barre de navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_profile);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                return true;
            } else if (id == R.id.nav_tasks) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_calendar) {
                Intent intent = new Intent(ProfileActivity.this, CalendarActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.action_logout) {
                SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
                prefs.edit().clear().apply();
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
            }

            return false;
        });
    }
}
