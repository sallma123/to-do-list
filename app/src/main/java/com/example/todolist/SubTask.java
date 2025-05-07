package com.example.todolist;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;

@Entity(
        tableName = "subtask_table",
        foreignKeys = @ForeignKey(
                entity = Task.class,
                parentColumns = "id",
                childColumns = "taskId",
                onDelete = ForeignKey.CASCADE
        )
)
public class SubTask {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public boolean isDone;
    public int taskId; // Clé étrangère vers Task

    public SubTask(String title, boolean isDone, int taskId) {
        this.title = title;
        this.isDone = isDone;
        this.taskId = taskId;
    }
}
