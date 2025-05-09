package com.example.todolist;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.List;

public class TaskDetailActivity extends AppCompatActivity {

    private EditText titleInput, descriptionInput;
    private TextView dateText;
    private LinearLayout subTaskLayout;
    private EditText newSubTaskInput;
    private Button addSubTaskButton;

    private TaskDao taskDao;
    private SubTaskDao subTaskDao;
    private Task task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        dateText = findViewById(R.id.dateText);
        subTaskLayout = findViewById(R.id.subTaskLayout);
        newSubTaskInput = findViewById(R.id.newSubTaskInput);
        addSubTaskButton = findViewById(R.id.addSubTaskButton);

        AppDatabase db = AppDatabase.getInstance(this);
        taskDao = db.taskDao();
        subTaskDao = db.subTaskDao();

        int taskId = getIntent().getIntExtra("task_id", -1);
        if (taskId == -1) finish();

        new Thread(() -> {
            task = taskDao.getTaskById(taskId);
            List<SubTask> subTasks = subTaskDao.getSubTasksForTask(taskId);

            runOnUiThread(() -> {
                if (task == null) return;

                titleInput.setText(task.getTitle());
                descriptionInput.setText(task.getDescription());
                if (task.date != null) {
                    dateText.setText(task.date);
                }
                displaySubTasks(subTasks);
            });
        }).start();

        titleInput.addTextChangedListener(new SimpleWatcher(() -> updateTask(titleInput.getText().toString(), null, null)));
        descriptionInput.addTextChangedListener(new SimpleWatcher(() -> updateTask(null, descriptionInput.getText().toString(), null)));

        dateText.setOnClickListener(v -> showDatePicker());

        addSubTaskButton.setOnClickListener(v -> {
            String title = newSubTaskInput.getText().toString().trim();
            if (!title.isEmpty()) {
                SubTask newSub = new SubTask(title, false, task.id);
                new Thread(() -> {
                    subTaskDao.insert(newSub);
                    List<SubTask> updated = subTaskDao.getSubTasksForTask(task.id);
                    runOnUiThread(() -> {
                        newSubTaskInput.setText("");
                        displaySubTasks(updated);
                    });
                }).start();
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            // ✅ Format standardisé avec zéro padding
            String selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day);
            dateText.setText(selectedDate);
            updateTask(null, null, selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }


    private void updateTask(String title, String desc, String date) {
        if (task == null) return;

        new Thread(() -> {
            if (title != null) task.setTitle(title);
            if (desc != null) task.setDescription(desc);
            if (date != null) task.date = date;

            taskDao.update(task);
        }).start();
    }

    private void displaySubTasks(List<SubTask> subTasks) {
        subTaskLayout.removeAllViews();
        for (SubTask sub : subTasks) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(sub.title);
            checkBox.setChecked(sub.isDone);
            checkBox.setOnCheckedChangeListener((btn, isChecked) -> {
                new Thread(() -> {
                    sub.isDone = isChecked;
                    subTaskDao.update(sub);
                }).start();
            });
            subTaskLayout.addView(checkBox);
        }
    }

    private static class SimpleWatcher implements TextWatcher {
        private final Runnable onChange;
        public SimpleWatcher(Runnable onChange) {
            this.onChange = onChange;
        }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            onChange.run();
        }
        @Override public void afterTextChanged(Editable s) {}
    }
}
