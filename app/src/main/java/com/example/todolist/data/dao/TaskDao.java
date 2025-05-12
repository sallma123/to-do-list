package com.example.todolist.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.example.todolist.data.model.Task;
import com.example.todolist.data.model.User;

import java.util.List;

// DAO pour accéder aux opérations de la table des tâches
@Dao
public interface TaskDao {

    // Insère une nouvelle tâche
    @Insert
    void insert(Task task);

    // Met à jour une tâche existante
    @Update
    void update(Task task);

    // Supprime une tâche
    @Delete
    void delete(Task task);

    // Récupère toutes les tâches (sans filtre utilisateur)
    @Query("SELECT * FROM task_table")
    List<Task> getAllTasks();

    // Récupère une tâche par son identifiant
    @Query("SELECT * FROM task_table WHERE id = :id LIMIT 1")
    Task getTaskById(int id);

    // Récupère toutes les tâches associées à un utilisateur
    @Query("SELECT * FROM task_table WHERE userId = :userId")
    List<Task> getTasksForUser(int userId);

    // Récupère les tâches d'un utilisateur pour une date donnée
    @Query("SELECT * FROM task_table WHERE userId = :userId AND date = :date")
    List<Task> getTasksForUserAndDate(int userId, String date);

    // Récupère un utilisateur par son identifiant (utilisé dans le profil)
    @Query("SELECT * FROM user_table WHERE id = :userId LIMIT 1")
    User getUserById(int userId);
}
