package com.example.todolist;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    private EditText signupName, signupEmail, signupPassword;
    private Button signupButton;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialiser les vues
        signupName = findViewById(R.id.signupName);
        signupEmail = findViewById(R.id.signupEmail);
        signupPassword = findViewById(R.id.signupPassword);
        signupButton = findViewById(R.id.signupButton);

        // Initialiser le UserDao
        userDao = AppDatabase.getInstance(this).userDao();

        // Bouton d'inscription
        signupButton.setOnClickListener(v -> {
            String name = signupName.getText().toString().trim();
            String email = signupEmail.getText().toString().trim();
            String password = signupPassword.getText().toString().trim();


            // Vérifie si tous les champs sont remplis
            if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                // Crée un utilisateur et l'ajoute à la base de données
                User newUser = new User(name, email, password);
                new Thread(() -> {
                    userDao.insert(newUser);
                    runOnUiThread(() -> {
                        Toast.makeText(SignUpActivity.this, "Inscription réussie", Toast.LENGTH_SHORT).show();
                        finish(); // Retourne à la page login
                    });
                }).start();
            } else {
                Toast.makeText(SignUpActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
