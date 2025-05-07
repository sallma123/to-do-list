package com.example.todolist;
import com.example.todolist.AppDatabase;  // Vérifie le bon chemin du package
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private Button loginButton, signUpButton;
    private UserDao userDao; // DAO pour accéder aux utilisateurs dans la base de données

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialiser les vues
        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton);

        // Initialiser le UserDao
        userDao = AppDatabase.getInstance(this).userDao();

        // Bouton de connexion
        loginButton.setOnClickListener(v -> {
            String email = loginEmail.getText().toString();
            String password = loginPassword.getText().toString();

            // Vérifiez si l'email et le mot de passe sont valides
            new Thread(() -> {
                boolean isValid = isValidLogin(email, password);

                runOnUiThread(() -> {
                    if (isValid) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Informations de connexion incorrectes", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();

        });

        // Bouton d'inscription
        signUpButton.setOnClickListener(v -> {
            // Ouvre la page d'inscription
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private boolean isValidLogin(String email, String password) {
        // Vérifiez si l'email et le mot de passe correspondent à un utilisateur dans la base de données
        User user = userDao.findByEmailAndPassword(email, password); // Requête personnalisée pour récupérer un utilisateur
        return user != null; // Si l'utilisateur est trouvé, les informations sont valides
    }
}
