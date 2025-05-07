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

    public Task(String title, String description, int userId) {
        this.title = title;
        this.description = description;
        this.userId = userId;
    }


    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
}
