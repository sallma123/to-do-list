package com.example.todolist.ui.auth;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.todolist.R;
import com.example.todolist.data.dao.UserDao;
import com.example.todolist.data.model.User;
import com.example.todolist.db.AppDatabase;

public class SignUpActivity extends AppCompatActivity {

    private EditText signupName, signupEmail, signupPassword;
    private Button signupButton;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialisation des vues
        signupName = findViewById(R.id.signupName);
        signupEmail = findViewById(R.id.signupEmail);
        signupPassword = findViewById(R.id.signupPassword);
        signupButton = findViewById(R.id.signupButton);

        // Initialisation du DAO utilisateur
        userDao = AppDatabase.getInstance(this).userDao();

        // Gestion du clic sur le bouton d'inscription
        signupButton.setOnClickListener(v -> {
            String name = signupName.getText().toString().trim();
            String email = signupEmail.getText().toString().trim();
            String password = signupPassword.getText().toString().trim();

            // Vérification des champs non vides
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Vérification de la validité de l'email
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(SignUpActivity.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            // Vérification de la longueur minimale du mot de passe
            if (password.length() < 4) {
                Toast.makeText(SignUpActivity.this, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // Création et insertion de l'utilisateur dans la base de données
            User newUser = new User(name, email, password);
            new Thread(() -> {
                userDao.insert(newUser);
                runOnUiThread(() -> {
                    Toast.makeText(SignUpActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    finish(); // Retour à l'écran de connexion
                });
            }).start();
        });
    }
}
