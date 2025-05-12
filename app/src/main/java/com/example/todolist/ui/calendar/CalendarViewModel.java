package com.example.todolist.ui.calendar;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.todolist.data.dao.TaskDao;
import com.example.todolist.data.model.Task;
import com.example.todolist.db.AppDatabase;

import java.util.List;
import java.util.concurrent.Executors;

//Gère la récupération des tâches pour une date spécifique.
public class CalendarViewModel extends AndroidViewModel {

    private final TaskDao taskDao;
    private final MutableLiveData<List<Task>> dailyTasksLiveData = new MutableLiveData<>();

    public CalendarViewModel(@NonNull Application application) {
        super(application);
        taskDao = AppDatabase.getInstance(application).taskDao();
    }

    public LiveData<List<Task>> getDailyTasks() {
        return dailyTasksLiveData;
    }

    public void loadTasksForDate(int userId, String date) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Task> tasks = taskDao.getTasksForUserAndDate(userId, date);
            dailyTasksLiveData.postValue(tasks);
        });
    }

    public boolean hasTasksForDate(int userId, String date) {
        List<Task> tasks = taskDao.getTasksForUserAndDate(userId, date);
        return tasks != null && !tasks.isEmpty();
    }
}
