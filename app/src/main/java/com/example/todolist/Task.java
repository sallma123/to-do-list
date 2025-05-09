package com.example.todolist;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "task_table")
public class Task {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String date;
    public int userId;
    public String title;
    public String description;
    public boolean isDone = false; // ✅ Ajouté pour indiquer si la tâche est terminée

    public Task(String title, String description, int userId, String date) {
        this.title = title;
        this.description = description;
        this.userId = userId;
        this.date = date;
    }



    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
