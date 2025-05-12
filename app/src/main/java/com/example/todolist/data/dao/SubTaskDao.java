package com.example.todolist.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.example.todolist.data.model.SubTask;

import java.util.List;

@Dao
public interface SubTaskDao {
    @Insert
    void insert(SubTask subTask);
    @Update
    void update(SubTask subTask);


    @Query("SELECT * FROM subtask_table WHERE taskId = :taskId")
    List<SubTask> getSubTasksForTask(int taskId);

    @Delete
    void delete(SubTask subTask);
}
