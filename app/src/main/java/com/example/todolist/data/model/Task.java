package com.example.todolist.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Entité représentant une tâche (table: task_table)
@Entity(tableName = "task_table")
public class Task {

    @PrimaryKey(autoGenerate = true)
    public int id;                  // Identifiant unique
    public String date;            // Date de la tâche
    public int userId;             // ID de l'utilisateur propriétaire
    public String title;           // Titre de la tâche
    public String description;     // Description de la tâche
    public boolean isDone = false; // Statut : terminée ou non

    public Task(String title, String description, int userId, String date) {
        this.title = title;
        this.description = description;
        this.userId = userId;
        this.date = date;
    }

    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public boolean isDone() { return isDone; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDate(String date) { this.date = date; }
}
