package com.example.todolist.ui.calendar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.utils.DayViewContainer;
import com.example.todolist.ui.auth.LoginActivity;
import com.example.todolist.ui.main.MainActivity;
import com.example.todolist.ui.profile.ProfileActivity;
import com.example.todolist.R;
import com.example.todolist.adapter.TaskAdapter;
import com.example.todolist.ui.detail.TaskDetailActivity;
import com.example.todolist.data.model.Task;
import com.example.todolist.db.AppDatabase;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kotlin.Unit;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private RecyclerView calendarTaskList;
    private TextView noTasksText, calendarTitle;
    private EditText taskInput;
    private Button addTaskButton;

    private TaskAdapter taskAdapter;
    private final List<Object> dailyTasks = new ArrayList<>();

    private int currentUserId;
    private String selectedDate;

    private final LocalDate today = LocalDate.now();
    private LocalDate selected = today;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    private CalendarViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Récupération de l'ID utilisateur
        currentUserId = getIntent().getIntExtra("user_id", -1);
        if (currentUserId == -1) {
            finish();
            return;
        }

        // Initialisation des vues
        calendarView = findViewById(R.id.calendarView);
        calendarTaskList = findViewById(R.id.calendarTaskList);
        noTasksText = findViewById(R.id.noTasksText);
        taskInput = findViewById(R.id.taskInputCalendar);
        addTaskButton = findViewById(R.id.addTaskButtonCalendar);
        calendarTitle = findViewById(R.id.calendarTitle);

        // Initialisation ViewModel
        viewModel = new ViewModelProvider(this).get(CalendarViewModel.class);

        // Configuration de l'adapter
        taskAdapter = new TaskAdapter(
                this,
                dailyTasks,
                task -> {
                    new Thread(() -> {
                        AppDatabase.getInstance(this).taskDao().delete(task);
                        runOnUiThread(() -> {
                            viewModel.loadTasksForDate(currentUserId, selected.toString());
                            calendarView.notifyCalendarChanged();
                        });
                    }).start();
                },
                task -> {
                    Intent intent = new Intent(CalendarActivity.this, TaskDetailActivity.class);
                    intent.putExtra("task_id", task.id);
                    startActivity(intent);
                },
                () -> {
                    viewModel.loadTasksForDate(currentUserId, selected.toString());
                    calendarView.notifyCalendarChanged();
                }
        );

        calendarTaskList.setLayoutManager(new LinearLayoutManager(this));
        calendarTaskList.setAdapter(taskAdapter);

        // Observer les tâches du jour sélectionné
        viewModel.getDailyTasks().observe(this, tasks -> {
            dailyTasks.clear();
            dailyTasks.addAll(tasks);
            taskAdapter.notifyDataSetChanged();
            calendarTaskList.setVisibility(tasks.isEmpty() ? View.GONE : View.VISIBLE);
            noTasksText.setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // Configuration du calendrier
        calendarView.setup(
                YearMonth.from(today).minusMonths(6),
                YearMonth.from(today).plusMonths(6),
                DayOfWeek.SUNDAY
        );
        calendarView.scrollToDate(today);
        calendarTitle.setText(today.format(formatter));

        calendarView.setMonthScrollListener((CalendarMonth month) -> {
            calendarTitle.setText(month.getYearMonth().format(formatter));
            return Unit.INSTANCE;
        });

        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @Override
            public DayViewContainer create(View view) {
                return new DayViewContainer(view, clickedDay -> {
                    if (clickedDay.getPosition() == DayPosition.MonthDate) {
                        selected = clickedDay.getDate();
                        selectedDate = selected.toString();
                        viewModel.loadTasksForDate(currentUserId, selectedDate);
                        calendarView.notifyCalendarChanged();
                    }
                });
            }

            @Override
            public void bind(DayViewContainer container, CalendarDay day) {
                container.bind(day);

                LocalDate date = day.getDate();
                boolean isToday = date.equals(today);
                boolean isSelected = date.equals(selected);

                Drawable bg = null;
                if (isToday) {
                    bg = ContextCompat.getDrawable(CalendarActivity.this, R.drawable.bg_today);
                } else if (isSelected) {
                    bg = ContextCompat.getDrawable(CalendarActivity.this, R.drawable.bg_selected);
                }
                container.dayText.setBackground(bg);

                // Affiche un point si des tâches existent pour ce jour
                new Thread(() -> {
                    boolean hasTask = AppDatabase.getInstance(CalendarActivity.this)
                            .taskDao()
                            .getTasksForUserAndDate(currentUserId, date.toString())
                            .size() > 0;

                    runOnUiThread(() -> container.dotView.setVisibility(hasTask ? View.VISIBLE : View.GONE));
                }).start();
            }
        });

        // Ajouter une tâche pour la date sélectionnée
        addTaskButton.setOnClickListener(v -> {
            String title = taskInput.getText().toString().trim();
            if (!title.isEmpty()) {
                Task newTask = new Task(title, "", currentUserId, selected.toString());
                new Thread(() -> {
                    AppDatabase.getInstance(this).taskDao().insert(newTask);
                    runOnUiThread(() -> {
                        taskInput.setText("");
                        viewModel.loadTasksForDate(currentUserId, selected.toString());
                        calendarView.notifyCalendarChanged();
                    });
                }).start();
            }
        });

        // Charger les tâches initiales
        viewModel.loadTasksForDate(currentUserId, selected.toString());

        // Barre de navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_calendar);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_tasks) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("user_id", currentUserId);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_calendar) {
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("user_id", currentUserId);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.action_logout) {
                SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
                prefs.edit().clear().apply();
                Intent intent = new Intent(this, LoginActivity.class);
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
        viewModel.loadTasksForDate(currentUserId, selected.toString());
        calendarView.notifyCalendarChanged();
    }
}
