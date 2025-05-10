package com.example.todolist;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_table") // Nom de la table dans la base de données
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id; // La clé primaire de l'entité

    public String name;

    public String email;
    public String password;

    // Constructeur
    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }
    public String getName() {
        return name;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
