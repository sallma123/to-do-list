package com.example.todolist.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.example.todolist.data.model.SubTask;

import java.util.List;

// DAO pour accéder à la table des sous-tâches (SubTask)
@Dao
public interface SubTaskDao {

    // Insère une nouvelle sous-tâche dans la base
    @Insert
    void insert(SubTask subTask);

    // Met à jour une sous-tâche existante
    @Update
    void update(SubTask subTask);

    // Récupère toutes les sous-tâches associées à une tâche principale
    @Query("SELECT * FROM subtask_table WHERE taskId = :taskId")
    List<SubTask> getSubTasksForTask(int taskId);

    // Supprime une sous-tâche
    @Delete
    void delete(SubTask subTask);
}
