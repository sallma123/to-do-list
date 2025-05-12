package com.example.todolist.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.todolist.data.dao.TaskDao;
import com.example.todolist.data.model.Task;
import com.example.todolist.db.AppDatabase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;

//Gère les données (liste de tâches formatée avec sections) de manière indépendante de l'UI
public class MainViewModel extends AndroidViewModel {

    private final TaskDao taskDao;
    private final MutableLiveData<List<Object>> tasksLiveData = new MutableLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
        taskDao = AppDatabase.getInstance(application).taskDao();
    }

    public LiveData<List<Object>> getTasksLiveData() {
        return tasksLiveData;
    }

    public void loadTasksForUser(int userId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Task> all = taskDao.getTasksForUser(userId);
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

            tasksLiveData.postValue(list);
        });
    }
}
