package com.example.todolist;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {User.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract UserDao userDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            // Créer l'instance de la base de données avec Room.databaseBuilder
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "user_database")
                    .fallbackToDestructiveMigration() // Si la migration échoue, recrée la base
                    .build();
        }
        return instance;
    }
}
