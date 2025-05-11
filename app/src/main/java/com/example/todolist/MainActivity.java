package com.example.todolist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Object> displayList;
    private Button addTaskButton;
    private EditText taskInput;
    private TaskDao taskDao;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Récupération de l'identifiant utilisateur depuis l'intent
        currentUserId = getIntent().getIntExtra("user_id", -1);
        if (currentUserId == -1) {
            finish(); // Aucun utilisateur connecté
            return;
        }

        // Accès à la base de données (DAO)
        taskDao = AppDatabase.getInstance(this).taskDao();

        // Initialisation des vues
        recyclerView = findViewById(R.id.recyclerView);
        taskInput = findViewById(R.id.taskInput);
        addTaskButton = findViewById(R.id.addTaskButton);

        // Initialisation de la liste affichée
        displayList = new ArrayList<>();
        taskAdapter = new TaskAdapter(this, displayList,
                task -> {
                    // Supprimer une tâche
                    new Thread(() -> {
                        taskDao.delete(task);
                        runOnUiThread(this::loadTasksFromDatabase);
                    }).start();
                },
                task -> {
                    // Afficher les détails d'une tâche
                    Intent intent = new Intent(MainActivity.this, TaskDetailActivity.class);
                    intent.putExtra("task_id", task.id);
                    startActivity(intent);
                },
                this::loadTasksFromDatabase // Mise à jour si tâche cochée
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);

        // Charger les tâches existantes
        loadTasksFromDatabase();

        // Ajouter une nouvelle tâche
        addTaskButton.setOnClickListener(v -> {
            String title = taskInput.getText().toString().trim();
            if (!title.isEmpty()) {
                Task newTask = new Task(title, "", currentUserId, LocalDate.now().toString());
                newTask.isDone = false;

                new Thread(() -> {
                    taskDao.insert(newTask);
                    runOnUiThread(() -> {
                        taskInput.setText("");
                        loadTasksFromDatabase();
                    });
                }).start();
            }
        });

        // Gestion du menu de navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_tasks); // Onglet sélectionné

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_tasks) {
                return true; // Déjà sur cet écran
            } else if (id == R.id.nav_calendar) {
                // Aller à l’écran calendrier
                Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
                intent.putExtra("user_id", currentUserId);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.action_logout) {
                // Déconnexion → on vide la session
                SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
                prefs.edit().clear().apply(); // Supprimer user_id

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                // Aller au profil
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra("user_id", currentUserId);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }

    //Charger les tâches depuis la base de données pour l’utilisateur courant.
    private void loadTasksFromDatabase() {
        new Thread(() -> {
            List<Task> all = taskDao.getTasksForUser(currentUserId);
            List<Object> list = new ArrayList<>();

            LocalDate today = LocalDate.now();
            List<Task> previous = new ArrayList<>();
            List<Task> future = new ArrayList<>();
            List<Task> completed = new ArrayList<>();

            for (Task t : all) {
                if (t.date == null) continue;
                LocalDate taskDate = LocalDate.parse(t.date);
                if (t.isDone) {
                    completed.add(t);
                } else if (taskDate.isBefore(today)) {
                    previous.add(t);
                } else {
                    future.add(t);
                }
            }

            // ✅ Tri croissant par date
            Comparator<Task> byDate = Comparator.comparing(task -> LocalDate.parse(task.date));
            previous.sort(byDate);
            future.sort(byDate);
            completed.sort(byDate);

            // Trie les tâches en : passées, à venir, complétées.
            if (!previous.isEmpty()) {
                list.add("Previous");
                list.addAll(previous);
            }

            if (!future.isEmpty()) {
                list.add("Future");
                list.addAll(future);
            }

            if (!completed.isEmpty()) {
                list.add("Completed");
                list.addAll(completed);
            }

            // Mise à jour de l’interface
            runOnUiThread(() -> {
                displayList.clear();
                displayList.addAll(list);
                taskAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Rechargement des tâches à chaque retour sur l’écran
        loadTasksFromDatabase();
    }
}
