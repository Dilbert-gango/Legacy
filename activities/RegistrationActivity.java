package com.example.legacy_order.activities; // Replace with your actual package name

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.legacy_order.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegistrationActivity extends AppCompatActivity {

    // UI Elements
    private EditText editTextName, editTextEmail, editTextPassword;
    private RadioGroup radioGroupRole;
    private RadioButton radioButtonBuyer, radioButtonSeller; // We might not need direct refs if just checking group
    private Button buttonRegister;
    private TextView textViewGoToLogin;
    private ProgressBar progressBarRegister;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase; // Reference to Firebase Realtime Database

    private static final String TAG = "RegistrationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize Firebase Auth & Database
        mAuth = FirebaseAuth.getInstance();
        // Get reference to the root of your Firebase Realtime Database
        // We'll store user info under a "Users" node
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Get references to UI elements
        editTextName = findViewById(R.id.editTextRegisterName);
        editTextEmail = findViewById(R.id.editTextRegisterEmail);
        editTextPassword = findViewById(R.id.editTextRegisterPassword);
        radioGroupRole = findViewById(R.id.radioGroupRole);
        radioButtonBuyer = findViewById(R.id.radioButtonBuyer); // Get refs if needed
        radioButtonSeller = findViewById(R.id.radioButtonSeller);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewGoToLogin = findViewById(R.id.textViewGoToLogin);
        progressBarRegister = findViewById(R.id.progressBarRegister);

        // Set OnClickListener for the Register button
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Set OnClickListener for the "Login here" text
        textViewGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to Login Activity
                // Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                // startActivity(intent);
                finish(); // Simply finish this activity to go back to Login
            }
        });
    }

    private void registerUser() {
        final String name = editTextName.getText().toString().trim();
        final String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        int selectedRoleId = radioGroupRole.getCheckedRadioButtonId();

        if (TextUtils.isEmpty(name)) {
            editTextName.setError("Name is required.");
            editTextName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required.");
            editTextEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email address.");
            editTextEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required.");
            editTextPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters long.");
            editTextPassword.requestFocus();
            return;
        }

        if (selectedRoleId == -1) {
            Toast.makeText(this, "Please select a role (Buyer/Seller)", Toast.LENGTH_SHORT).show();
            return;
        }

        final String role = ((RadioButton) findViewById(selectedRoleId)).getText().toString();

        progressBarRegister.setVisibility(View.VISIBLE);
        buttonRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String userId = firebaseUser.getUid();

                                Map<String, Object> userData = new HashMap<>();
                                userData.put("name", name);
                                userData.put("email", email);
                                userData.put("role", role); // THIS LINE does the magic

                                mDatabase.child("Users").child(userId).setValue(userData)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressBarRegister.setVisibility(View.GONE);
                                                buttonRegister.setEnabled(true);
                                                Toast.makeText(RegistrationActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                                                // Go to Login or wherever
                                                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressBarRegister.setVisibility(View.GONE);
                                                buttonRegister.setEnabled(true);
                                                Toast.makeText(RegistrationActivity.this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                                                Log.e(TAG, "Data save failed: ", e);
                                            }
                                        });
                            }
                        } else {
                            progressBarRegister.setVisibility(View.GONE);
                            buttonRegister.setEnabled(true);
                            Toast.makeText(RegistrationActivity.this, "Registration failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

}
