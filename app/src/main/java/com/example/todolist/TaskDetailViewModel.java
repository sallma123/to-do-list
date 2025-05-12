package com.example.todolist;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * ViewModel pour TaskDetailActivity.
 * Gère la tâche principale et ses sous-tâches via LiveData.
 */
public class TaskDetailViewModel extends AndroidViewModel {

    private final TaskDao taskDao;
    private final SubTaskDao subTaskDao;

    private final MutableLiveData<Task> taskLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<SubTask>> subTasksLiveData = new MutableLiveData<>();

    public TaskDetailViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        taskDao = db.taskDao();
        subTaskDao = db.subTaskDao();
    }

    public LiveData<Task> getTask() {
        return taskLiveData;
    }

    public LiveData<List<SubTask>> getSubTasks() {
        return subTasksLiveData;
    }

    public void loadTask(int taskId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Task task = taskDao.getTaskById(taskId);
            List<SubTask> subs = subTaskDao.getSubTasksForTask(taskId);
            taskLiveData.postValue(task);
            subTasksLiveData.postValue(subs);
        });
    }

    public void updateTaskFields(String title, String description, String date) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Task task = taskLiveData.getValue();
            if (task == null) return;

            if (title != null) task.setTitle(title);
            if (description != null) task.setDescription(description);
            if (date != null) task.date = date;

            taskDao.update(task);
        });
    }

    public void addSubTask(String title) {
        Task task = taskLiveData.getValue();
        if (task == null || title.isEmpty()) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            subTaskDao.insert(new SubTask(title, false, task.id));
            List<SubTask> updated = subTaskDao.getSubTasksForTask(task.id);
            subTasksLiveData.postValue(updated);
        });
    }

    public void updateSubTaskStatus(SubTask subTask, boolean isDone) {
        Executors.newSingleThreadExecutor().execute(() -> {
            subTask.isDone = isDone;
            subTaskDao.update(subTask);
        });
    }
}
