package com.example.todolist;

public class Task {
    private String title;  // Titre de la tâche
    private String description;  // Description de la tâche

    // Constructeur pour initialiser les valeurs
    public Task(String title, String description) {
        this.title = title;
        this.description = description;
    }

    // Getter pour obtenir le titre de la tâche
    public String getTitle() {
        return title;
    }

    // Getter pour obtenir la description de la tâche
    public String getDescription() {
        return description;
    }

    // Setter pour définir un nouveau titre pour la tâche (si nécessaire)
    public void setTitle(String title) {
        this.title = title;
    }

    // Setter pour définir une nouvelle description pour la tâche (si nécessaire)
    public void setDescription(String description) {
        this.description = description;
    }
}
