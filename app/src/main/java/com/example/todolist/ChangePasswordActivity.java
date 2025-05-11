package com.example.todolist;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.Executors;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText oldPasswordInput, newPasswordInput, confirmPasswordInput;
    private Button savePasswordButton;
    private UserDao userDao;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Initialisation des vues
        oldPasswordInput = findViewById(R.id.oldPasswordInput);
        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        savePasswordButton = findViewById(R.id.savePasswordButton);

        // Accès à la base de données
        userDao = AppDatabase.getInstance(this).userDao();
        userId = getIntent().getIntExtra("user_id", -1);

        // Gestion du clic sur le bouton de sauvegarde
        savePasswordButton.setOnClickListener(v -> {
            String oldPass = oldPasswordInput.getText().toString().trim();
            String newPass = newPasswordInput.getText().toString().trim();
            String confirmPass = confirmPasswordInput.getText().toString().trim();

            // Vérifie que tous les champs sont remplis
            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Vérifie que le mot de passe fait au moins 4 caractères
            if (newPass.length() < 4) {
                Toast.makeText(this, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // Vérifie que les mots de passe sont identiques
            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Vérifie que le nouveau mot de passe est différent de l'ancien
            if (oldPass.equals(newPass)) {
                Toast.makeText(this, "New password must be different from current password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Mise à jour du mot de passe
            Executors.newSingleThreadExecutor().execute(() -> {
                User user = userDao.getUserById(userId);
                if (user != null && user.getPassword().equals(oldPass)) {
                    user.setPassword(newPass);
                    userDao.update(user);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show()
                    );
                }
            });
        });
    }
}
