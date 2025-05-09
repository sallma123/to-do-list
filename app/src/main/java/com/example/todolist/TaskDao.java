package com.example.todolist;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    void insert(Task task);
    @Update
    void update(Task task);


    @Query("SELECT * FROM task_table")
    List<Task> getAllTasks();

    @Delete
    void delete(Task task);

    @Query("SELECT * FROM task_table WHERE id = :id LIMIT 1")
    Task getTaskById(int id);
    @Query("SELECT * FROM task_table WHERE userId = :userId")
    List<Task> getTasksForUser(int userId);

}

