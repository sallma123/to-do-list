package com.example.todolist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.todolist.R; // ✅ celui-ci

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private Button addTaskButton;
    private EditText taskInput;
    private TaskDao taskDao;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ✅ Récupérer l'ID utilisateur depuis LoginActivity
        currentUserId = getIntent().getIntExtra("user_id", -1);
        if (currentUserId == -1) {
            finish(); // Quitte si aucun ID n'est transmis
            return;
        }

        // Initialisation DAO
        taskDao = AppDatabase.getInstance(this).taskDao();

        // Vues
        recyclerView = findViewById(R.id.recyclerView);
        taskInput = findViewById(R.id.taskInput);
        addTaskButton = findViewById(R.id.addTaskButton);

        // Liste des tâches + adapter
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList,
                task -> {
                    // Suppression
                    new Thread(() -> {
                        taskDao.delete(task);
                        runOnUiThread(this::loadTasksFromDatabase);
                    }).start();
                },
                task -> {
                    // Clic simple → ouvrir la page détail
                    Intent intent = new Intent(MainActivity.this, TaskDetailActivity.class);
                    intent.putExtra("task_id", task.id);
                    startActivity(intent);
                }
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);

        // Charger les tâches existantes
        loadTasksFromDatabase();

        // Ajouter une tâche
        addTaskButton.setOnClickListener(v -> {
            String title = taskInput.getText().toString().trim();

            if (!title.isEmpty()) {
                Task newTask = new Task(title, "", currentUserId);

                new Thread(() -> {
                    taskDao.insert(newTask);
                    runOnUiThread(() -> {
                        taskInput.setText("");
                        loadTasksFromDatabase();
                    });
                }).start();
            }
        });

        // ✅ Gestion de la barre de navigation en bas
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        // 👇 Définit "Tasks" comme sélectionné par défaut
        bottomNav.setSelectedItemId(R.id.nav_tasks);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_logout) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_tasks) {
                return true;
            } else if (id == R.id.nav_calendar) {
                Toast.makeText(this, "Calendrier à venir", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_profile) {
                Toast.makeText(this, "Profil à venir", Toast.LENGTH_SHORT).show();
                return true;
            }

            return false;
        });
    }

        private void loadTasksFromDatabase() {
        new Thread(() -> {
            List<Task> tasks = taskDao.getTasksForUser(currentUserId);
            runOnUiThread(() -> {
                taskList.clear();
                taskList.addAll(tasks);
                taskAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasksFromDatabase(); // recharge les données au retour
    }
}
