package com.example.todolist.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.todolist.data.dao.SubTaskDao;
import com.example.todolist.data.dao.TaskDao;
import com.example.todolist.data.dao.UserDao;
import com.example.todolist.data.model.SubTask;
import com.example.todolist.data.model.Task;
import com.example.todolist.data.model.User;

@Database(entities = {User.class, Task.class, SubTask.class}, version = 5, exportSchema = false)


public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract UserDao userDao();
    public abstract TaskDao taskDao();
    public abstract SubTaskDao subTaskDao();
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            // Créer l'instance de la base de données avec Room.databaseBuilder
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "user_db")
                    .fallbackToDestructiveMigration() // Si la migration échoue, recrée la base
                    .build();
        }
        return instance;
    }
}
