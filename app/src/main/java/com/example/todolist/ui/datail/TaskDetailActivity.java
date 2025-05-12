package com.example.todolist.ui.datail;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.todolist.R;
import com.example.todolist.data.model.SubTask;

import java.util.Calendar;
import java.util.List;

public class TaskDetailActivity extends AppCompatActivity {

    private EditText titleInput, descriptionInput;
    private TextView dateText;
    private LinearLayout subTaskLayout;
    private EditText newSubTaskInput;
    private Button addSubTaskButton;

    private TaskDetailViewModel viewModel;
    private int taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        // Initialisation des vues
        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        dateText = findViewById(R.id.dateText);
        subTaskLayout = findViewById(R.id.subTaskLayout);
        newSubTaskInput = findViewById(R.id.newSubTaskInput);
        addSubTaskButton = findViewById(R.id.addSubTaskButton);

        // Récupération de l’ID de la tâche
        taskId = getIntent().getIntExtra("task_id", -1);
        if (taskId == -1) finish();

        // Initialiser le ViewModel
        viewModel = new ViewModelProvider(this).get(TaskDetailViewModel.class);

        // Observer les données de la tâche
        viewModel.getTask().observe(this, task -> {
            if (task == null) return;
            titleInput.setText(task.getTitle());
            descriptionInput.setText(task.getDescription());
            if (task.date != null) {
                dateText.setText(task.date);
            }
        });

        // Observer les sous-tâches
        viewModel.getSubTasks().observe(this, this::displaySubTasks);

        // Charger la tâche
        viewModel.loadTask(taskId);

        // Modifications en temps réel
        titleInput.addTextChangedListener(new SimpleWatcher(() ->
                viewModel.updateTaskFields(titleInput.getText().toString(), null, null)));

        descriptionInput.addTextChangedListener(new SimpleWatcher(() ->
                viewModel.updateTaskFields(null, descriptionInput.getText().toString(), null)));

        dateText.setOnClickListener(v -> showDatePicker());

        // Ajouter une sous-tâche
        addSubTaskButton.setOnClickListener(v -> {
            String title = newSubTaskInput.getText().toString().trim();
            if (!title.isEmpty()) {
                viewModel.addSubTask(title);
                newSubTaskInput.setText("");
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day);
            dateText.setText(selectedDate);
            viewModel.updateTaskFields(null, null, selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void displaySubTasks(List<SubTask> subTasks) {
        subTaskLayout.removeAllViews();
        for (SubTask sub : subTasks) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(sub.title);
            checkBox.setChecked(sub.isDone);
            checkBox.setOnCheckedChangeListener((btn, isChecked) ->
                    viewModel.updateSubTaskStatus(sub, isChecked));
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
