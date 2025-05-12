package com.example.todolist.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;

// Entité représentant une sous-tâche liée à une tâche principale
@Entity(
        tableName = "subtask_table",
        foreignKeys = @ForeignKey(
                entity = Task.class,             // Entité parent : Task
                parentColumns = "id",            // Clé primaire dans Task
                childColumns = "taskId",         // Clé étrangère dans SubTask
                onDelete = ForeignKey.CASCADE    // Supprime les sous-tâches si la tâche est supprimée
        )
)
public class SubTask {

    @PrimaryKey(autoGenerate = true)
    public int id;          // Identifiant unique de la sous-tâche

    public String title;    // Titre de la sous-tâche
    public boolean isDone;  // Indique si la sous-tâche est terminée
    public int taskId;      // Référence à la tâche principale (clé étrangère)

    public SubTask(String title, boolean isDone, int taskId) {
        this.title = title;
        this.isDone = isDone;
        this.taskId = taskId;
    }
}
