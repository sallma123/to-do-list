package com.example.todolist.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.ui.profile.ProfileActivity;
import com.example.todolist.R;
import com.example.todolist.adapter.TaskAdapter;
import com.example.todolist.ui.datail.TaskDetailActivity;
import com.example.todolist.network.WeatherApiService;
import com.example.todolist.network.WeatherClient;
import com.example.todolist.network.WeatherResponse;
import com.example.todolist.data.model.Task;
import com.example.todolist.db.AppDatabase;
import com.example.todolist.ui.auth.LoginActivity;
import com.example.todolist.ui.calendar.CalendarActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDate;
import java.util.ArrayList;
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
    private TextView weatherText;

    private int currentUserId;
    private MainViewModel viewModel;

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

        // Initialisation des vues
        weatherText = findViewById(R.id.weatherText);
        recyclerView = findViewById(R.id.recyclerView);
        taskInput = findViewById(R.id.taskInput);
        addTaskButton = findViewById(R.id.addTaskButton);

        // Initialisation de la liste et de l'adapter
        displayList = new ArrayList<>();
        taskAdapter = new TaskAdapter(this, displayList,
                task -> {
                    new Thread(() -> {
                        AppDatabase.getInstance(this).taskDao().delete(task);
                        viewModel.loadTasksForUser(currentUserId);
                    }).start();
                },
                task -> {
                    Intent intent = new Intent(MainActivity.this, TaskDetailActivity.class);
                    intent.putExtra("task_id", task.id);
                    startActivity(intent);
                },
                () -> viewModel.loadTasksForUser(currentUserId)
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);

        // Initialisation du ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getTasksLiveData().observe(this, tasks -> {
            displayList.clear();
            displayList.addAll(tasks);
            taskAdapter.notifyDataSetChanged();
        });

        // Charger les tâches via ViewModel
        viewModel.loadTasksForUser(currentUserId);

        // Ajouter une tâche
        addTaskButton.setOnClickListener(v -> {
            String title = taskInput.getText().toString().trim();
            if (!title.isEmpty()) {
                Task newTask = new Task(title, "", currentUserId, LocalDate.now().toString());
                newTask.isDone = false;

                new Thread(() -> {
                    AppDatabase.getInstance(this).taskDao().insert(newTask);
                    runOnUiThread(() -> {
                        taskInput.setText("");
                        viewModel.loadTasksForUser(currentUserId);
                    });
                }).start();
            }
        });

        // Appel météo
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
                        weatherText.setText("Error fetching weather.");
                    }
                });

        // Navigation
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

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadTasksForUser(currentUserId);
    }
}
