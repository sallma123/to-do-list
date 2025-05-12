package com.example.todolist.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.todolist.data.dao.TaskDao;
import com.example.todolist.data.dao.UserDao;
import com.example.todolist.data.model.Task;
import com.example.todolist.data.model.User;
import com.example.todolist.db.AppDatabase;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * ViewModel pour ProfileActivity.
 * Gère les données utilisateur et statistiques sans accès direct à Room dans l'UI.
 */
public class ProfileViewModel extends AndroidViewModel {

    private final UserDao userDao;
    private final TaskDao taskDao;

    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<Stats> statsLiveData = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
        taskDao = db.taskDao();
    }

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<Stats> getStatsLiveData() {
        return statsLiveData;
    }

    public void loadUser(int userId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            User user = userDao.getUserById(userId);
            userLiveData.postValue(user);
        });
    }

    public void loadStats(int userId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Task> tasks = taskDao.getTasksForUser(userId);
            int total = tasks.size();
            int completed = 0;
            for (Task task : tasks) {
                if (task.isDone()) completed++;
            }
            int rate = total == 0 ? 0 : (completed * 100 / total);
            statsLiveData.postValue(new Stats(total, rate));
        });
    }

    // Classe interne pour représenter les stats
    public static class Stats {
        public final int total;
        public final int completionRate;

        public Stats(int total, int completionRate) {
            this.total = total;
            this.completionRate = completionRate;
        }
    }
}
