package com.example.todolist.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Entité représentant un utilisateur (table: user_table)
@Entity(tableName = "user_table")
public class User {

    @PrimaryKey(autoGenerate = true)
    public int id;          // Identifiant unique de l'utilisateur
    public String name;     // Nom de l'utilisateur
    public String email;    // Adresse email (utilisée pour la connexion)
    public String password; // Mot de passe

    // Constructeur
    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // Getters
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }

    // Setters
    public void setPassword(String password) { this.password = password; }
}
