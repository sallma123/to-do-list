package com.example.todolist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.graphics.drawable.Drawable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private TextView noTasksText;
    private TextView calendarTitle;
    private EditText taskInput;
    private Button addTaskButton;

    private TaskAdapter taskAdapter;
    private final List<Task> dailyTasks = new ArrayList<>();

    private TaskDao taskDao;
    private int currentUserId;
    private String selectedDate;

    private LocalDate today = LocalDate.now();
    private LocalDate selected = today;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        currentUserId = getIntent().getIntExtra("user_id", -1);
        if (currentUserId == -1) {
            finish();
            return;
        }

        calendarView = findViewById(R.id.calendarView);
        calendarTaskList = findViewById(R.id.calendarTaskList);
        noTasksText = findViewById(R.id.noTasksText);
        taskInput = findViewById(R.id.taskInputCalendar);
        addTaskButton = findViewById(R.id.addTaskButtonCalendar);
        calendarTitle = findViewById(R.id.calendarTitle);

        taskDao = AppDatabase.getInstance(this).taskDao();

        taskAdapter = new TaskAdapter(dailyTasks,
                task -> {
                    new Thread(() -> {
                        taskDao.delete(task);
                        runOnUiThread(() -> {
                            loadTasksForDate(selected.toString());
                            calendarView.notifyCalendarChanged(); // 🔄 rafraîchit les points
                        });
                    }).start();
                },
                task -> {
                    Intent intent = new Intent(CalendarActivity.this, TaskDetailActivity.class);
                    intent.putExtra("task_id", task.id);
                    startActivity(intent);
                });


        calendarTaskList.setLayoutManager(new LinearLayoutManager(this));
        calendarTaskList.setAdapter(taskAdapter);

        calendarView.setup(
                YearMonth.from(today).minusMonths(6),
                YearMonth.from(today).plusMonths(6),
                DayOfWeek.MONDAY
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
                        loadTasksForDate(selectedDate);
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

                new Thread(() -> {
                    boolean hasTask = !taskDao.getTasksForUserAndDate(currentUserId, date.toString()).isEmpty();
                    runOnUiThread(() -> {
                        container.dotView.setVisibility(hasTask ? View.VISIBLE : View.GONE);
                    });
                }).start();
            }
        });

        addTaskButton.setOnClickListener(v -> {
            String title = taskInput.getText().toString().trim();
            if (!title.isEmpty()) {
                Task newTask = new Task(title, "", currentUserId, selected.toString());
                newTask.date = selected.toString();
                new Thread(() -> {
                    taskDao.insert(newTask);
                    runOnUiThread(() -> {
                        taskInput.setText("");
                        loadTasksForDate(selected.toString());
                        calendarView.notifyCalendarChanged();
                    });
                }).start();
            }
        });

        loadTasksForDate(selected.toString());

        // ✅ Navigation bar handling
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_calendar);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_tasks) {
                Intent intent = new Intent(CalendarActivity.this, MainActivity.class);
                intent.putExtra("user_id", currentUserId);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.action_logout) {
                startActivity(new Intent(CalendarActivity.this, LoginActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_calendar) {
                return true; // déjà ici
            } else if (id == R.id.nav_profile) {
                // Ajoute la redirection vers l’écran du profil si nécessaire
                return true;
            }
            return false;
        });
    }

    public void loadTasksForDate(String date) {
        new Thread(() -> {
            List<Task> tasks = taskDao.getTasksForUserAndDate(currentUserId, date);
            runOnUiThread(() -> {
                dailyTasks.clear();
                dailyTasks.addAll(tasks);
                taskAdapter.notifyDataSetChanged();
                calendarTaskList.setVisibility(tasks.isEmpty() ? RecyclerView.GONE : View.VISIBLE);
                noTasksText.setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }).start();
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadTasksForDate(selected.toString()); // ✅ recharge les tâches du jour sélectionné
        calendarView.notifyCalendarChanged(); // ✅ met à jour les points dans le calendrier
    }

}
