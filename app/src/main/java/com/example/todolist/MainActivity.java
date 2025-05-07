package com.example.todolist;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private ArrayList<Task> taskList;
    private Button addTaskButton;
    private EditText taskInput; // EditText pour saisir une tâche

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisation du RecyclerView et de l'adaptateur
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList);
        recyclerView.setAdapter(taskAdapter);

        // Initialisation de l'EditText et du bouton "Add Task"
        taskInput = findViewById(R.id.taskInput);  // Champ pour entrer une nouvelle tâche
        addTaskButton = findViewById(R.id.addTaskButton);

        // Gérer le clic sur le bouton "Add Task"
        addTaskButton.setOnClickListener(v -> {
            String taskTitle = taskInput.getText().toString().trim();  // Récupérer le texte saisi

            if (!taskTitle.isEmpty()) {  // Vérifier que la tâche n'est pas vide
                // Ajouter la nouvelle tâche à la liste
                taskList.add(new Task(taskTitle, "Description for " + taskTitle));
                taskAdapter.notifyItemInserted(taskList.size() - 1);  // Informer l'adaptateur du changement
                taskInput.setText("");  // Réinitialiser le champ de texte après ajout
            }
        });
    }
}
