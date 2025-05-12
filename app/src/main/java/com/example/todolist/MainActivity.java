package com.example.todolist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Object> displayList;
    private Button addTaskButton;
    private EditText taskInput;
    private TaskDao taskDao;
    private int currentUserId;
    private TextView weatherText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Récupération de l'identifiant utilisateur
        currentUserId = getIntent().getIntExtra("user_id", -1);
        if (currentUserId == -1) {
            finish();
            return;
        }

        // DAO
        taskDao = AppDatabase.getInstance(this).taskDao();

        // Vues
        weatherText = findViewById(R.id.weatherText);
        recyclerView = findViewById(R.id.recyclerView);
        taskInput = findViewById(R.id.taskInput);
        addTaskButton = findViewById(R.id.addTaskButton);

        // Adapter
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
                },
                this::loadTasksFromDatabase
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);

        // Charger les tâches
        loadTasksFromDatabase();

        // Ajouter une tâche
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

        // Appel API météo pour Rabat
        WeatherApiService service = WeatherClient.getService();
        service.getWeather("Rabat,MA", "metric", "ecd9a22ca89fc0b7ca2a6e517bac6dec")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            double temp = response.body().main.temp;
                            String description = response.body().weather.get(0).description;
                            String display = String.format("Rabat: %.0f°C, %s", temp, description);
                            weatherText.setText(display);
                        } else {
                            weatherText.setText("Unable to load weather.");
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        Log.e("WEATHER_API", "Erreur Retrofit : " + t.getMessage());
                        t.printStackTrace(); 
                    }

                });

        // Barre de navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_tasks);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_tasks) {
                return true;
            } else if (id == R.id.nav_calendar) {
                Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
                intent.putExtra("user_id", currentUserId);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.action_logout) {
                SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
                prefs.edit().clear().apply();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra("user_id", currentUserId);
                startActivity(intent);
                finish();
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

            Comparator<Task> byDate = Comparator.comparing(task -> LocalDate.parse(task.date));
            previous.sort(byDate);
            future.sort(byDate);
            completed.sort(byDate);

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
