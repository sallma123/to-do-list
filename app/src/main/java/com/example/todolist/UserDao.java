package com.example.todolist;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    void insert(User user);

    @Query("SELECT * FROM user_table WHERE email = :email AND password = :password LIMIT 1")
    User findByEmailAndPassword(String email, String password); // Méthode pour trouver un utilisateur par email et mot de passe

    @Query("SELECT * FROM user_table")
    List<User> getAllUsers(); // Méthode pour récupérer tous les utilisateurs
    @Query("SELECT * FROM user_table WHERE id = :userId LIMIT 1")
    User getUserById(int userId);

}
