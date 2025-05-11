package com.example.todolist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private Button loginButton, signUpButton;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Vérifie si une session est déjà enregistrée dans SharedPreferences
        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        int savedUserId = prefs.getInt("user_id", -1);
        if (savedUserId != -1) {
            // Redirige automatiquement vers MainActivity si l'utilisateur est déjà connecté
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("user_id", savedUserId);
            startActivity(intent);
            finish();
            return;
        }

        // Initialisation des vues
        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton);

        // Initialisation de l’accès à la base de données (DAO)
        userDao = AppDatabase.getInstance(this).userDao();

        // Gérer la connexion quand on clique sur "Login"
        loginButton.setOnClickListener(v -> {
            String email = loginEmail.getText().toString();
            String password = loginPassword.getText().toString();

            // Thread pour requête Room (éviter blocage UI)
            new Thread(() -> {
                User user = userDao.findByEmailAndPassword(email, password);

                runOnUiThread(() -> {
                    if (user != null) {
                        // Sauvegarde de la session avec SharedPreferences
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("user_id", user.id);
                        editor.apply();

                        // Lancement de l'activité principale avec l'ID utilisateur
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("user_id", user.id);
                        startActivity(intent);
                        finish();
                    } else {
                        // Erreur de connexion
                        Toast.makeText(LoginActivity.this, "Incorrect credentials", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });

        // Rediriger vers l'écran d'inscription
        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
}
