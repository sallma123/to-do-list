package com.example.todolist;

import android.content.Intent;
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

        currentUserId = getIntent().getIntExtra("user_id", -1);
        if (currentUserId == -1) {
            finish();
            return;
        }

        taskDao = AppDatabase.getInstance(this).taskDao();

        recyclerView = findViewById(R.id.recyclerView);
        taskInput = findViewById(R.id.taskInput);
        addTaskButton = findViewById(R.id.addTaskButton);

        displayList = new ArrayList<>();
        taskAdapter = new TaskAdapter(this, displayList,
                task -> {
                    new Thread(() -> {
                        taskDao.delete(task);
                        runOnUiThread(this::loadTasksFromDatabase);
                    }).start();
                },
                task -> {
                    Intent intent = new Intent(MainActivity.this, TaskDetailActivity.class);
                    intent.putExtra("task_id", task.id);
                    startActivity(intent);
                });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);

        loadTasksFromDatabase();

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

        // ✅ Gestion de la barre de navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_tasks); // Définit l'onglet sélectionné

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_tasks) {
                return true; // Déjà sur cette page
            } else if (id == R.id.nav_calendar) {
                Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
                intent.putExtra("user_id", currentUserId);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.action_logout) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
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
        loadTasksFromDatabase();
    }
}
