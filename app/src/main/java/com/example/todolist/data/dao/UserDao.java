package com.example.todolist.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.todolist.data.model.User;

import java.util.List;

// DAO pour gérer les opérations liées à la table des utilisateurs
@Dao
public interface UserDao {

    // Insère un nouvel utilisateur
    @Insert
    void insert(User user);

    // Met à jour les informations d'un utilisateur
    @Update
    void update(User user);

    // Recherche un utilisateur par email et mot de passe (connexion)
    @Query("SELECT * FROM user_table WHERE email = :email AND password = :password LIMIT 1")
    User findByEmailAndPassword(String email, String password);

    // Récupère la liste de tous les utilisateurs
    @Query("SELECT * FROM user_table")
    List<User> getAllUsers();

    // Récupère un utilisateur par son identifiant
    @Query("SELECT * FROM user_table WHERE id = :userId LIMIT 1")
    User getUserById(int userId);
}
