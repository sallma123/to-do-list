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
    private List<Task> taskList;
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

        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList,
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
                }
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);

        loadTasksFromDatabase();

        addTaskButton.setOnClickListener(v -> {
            String title = taskInput.getText().toString().trim();

            if (!title.isEmpty()) {
                String today = LocalDate.now().toString(); // ✅ Date du jour
                Task newTask = new Task(title, "", currentUserId, today);
                newTask.date = today; // ✅ affectée ici

                new Thread(() -> {
                    taskDao.insert(newTask);
                    runOnUiThread(() -> {
                        taskInput.setText("");
                        loadTasksFromDatabase();
                    });
                }).start();
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
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
                Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
                intent.putExtra("user_id", currentUserId);
                startActivity(intent);
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
        loadTasksFromDatabase();
    }
}
