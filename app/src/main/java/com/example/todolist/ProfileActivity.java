package com.example.todolist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    private TextView emailText, nameText, totalTasks, completionRate;
    private Button changePasswordButton;

    private AppDatabase db;
    private UserDao userDao;
    private TaskDao taskDao;
    private int userId; // à recevoir via Intent

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

        // Accès à la base de données
        db = AppDatabase.getInstance(this);
        userDao = db.userDao();
        taskDao = db.taskDao();

        // Récupérer l'ID utilisateur depuis l'Intent
        userId = getIntent().getIntExtra("user_id", -1);

        if (userId != -1) {
            loadUserInfo();     // Charger l'email et le nom
            loadTaskStats();    // Charger les statistiques
        }

        // Bouton pour changer le mot de passe
        changePasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });

        // Barre de navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_profile); // Onglet actif

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                return true; // Déjà sur cette page
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
                // Déconnexion : on efface la session
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

    //Récupère les infos de l'utilisateur (email, nom) depuis Room
    private void loadUserInfo() {
        Executors.newSingleThreadExecutor().execute(() -> {
            User user = userDao.getUserById(userId);
            runOnUiThread(() -> {
                if (user != null) {
                    emailText.setText("Email: " + user.getEmail());
                    nameText.setText("Name: " + (user.getName() != null ? user.getName() : "(not set)"));
                }
            });
        });
    }

    //Calcule les statistiques de l'utilisateur (total de tâches et % complétées)
    private void loadTaskStats() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Task> tasks = taskDao.getTasksForUser(userId);
            int total = tasks.size();
            int completed = 0;
            for (Task task : tasks) {
                if (task.isDone()) completed++;
            }

            int percentage = total == 0 ? 0 : (completed * 100 / total);
            runOnUiThread(() -> {
                totalTasks.setText(String.valueOf(total));
                completionRate.setText(percentage + "%");
            });
        });
    }
}
