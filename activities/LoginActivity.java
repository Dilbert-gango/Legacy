package com.example.legacy_order.activities; // Using your package name

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
import android.widget.TextView;
import android.widget.Toast;

import com.example.legacy_order.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity {

    // UI Elements
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textViewGoToRegister;
    private ProgressBar progressBarLogin;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsersRef; // Reference to "Users" node

    private static final String TAG = "LoginActivity"; // For logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth & Database Ref
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Get references to UI elements
        editTextEmail = findViewById(R.id.editTextLoginEmail);
        editTextPassword = findViewById(R.id.editTextLoginPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewGoToRegister = findViewById(R.id.textViewGoToRegister);
        progressBarLogin = findViewById(R.id.progressBarLogin);

        // Set OnClickListener for the Login button
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Set OnClickListener for the "Register here" text
        textViewGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Registration Activity
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // *** CORRECTED onStart LOGIC ***
        // Check if user is signed in (non-null) and redirect immediately
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Log.d(TAG, "User already logged in: " + currentUser.getUid() + ". Checking role...");
            // User is logged in, check their role and redirect
            // Show progress bar while checking role
            progressBarLogin.setVisibility(View.VISIBLE);
            buttonLogin.setEnabled(false);
            textViewGoToRegister.setEnabled(false);
            navigateToHome(currentUser); // Call your navigation method
        } else {
            Log.d(TAG, "No user logged in.");
            // Ensure UI is in correct state if no user is logged in
            progressBarLogin.setVisibility(View.GONE);
            buttonLogin.setEnabled(true);
            textViewGoToRegister.setEnabled(true);
        }
        // *** END OF CORRECTED onStart LOGIC ***
    }


    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // --- Input Validation ---
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
        // --- End Input Validation ---

        progressBarLogin.setVisibility(View.VISIBLE);
        buttonLogin.setEnabled(false);
        textViewGoToRegister.setEnabled(false); // Disable register link during login attempt

        // --- Firebase Sign In ---
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // No need to hide progress bar here if successful,
                        // as navigateToHome will handle it or finish the activity.

                        if (task.isSuccessful()) {
                            // Sign in success
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Navigate based on role fetched from DB
                                navigateToHome(user);
                            } else {
                                // Should not happen, but handle defensively
                                Log.w(TAG, "signInWithEmail:success but user is null!");
                                progressBarLogin.setVisibility(View.GONE);
                                buttonLogin.setEnabled(true);
                                textViewGoToRegister.setEnabled(true);
                                Toast.makeText(LoginActivity.this, "Login successful, but failed to get user details.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            progressBarLogin.setVisibility(View.GONE);
                            buttonLogin.setEnabled(true);
                            textViewGoToRegister.setEnabled(true);
                            Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
        // --- End Firebase Sign In ---
    }

    /**
     * Fetches the user's role from Firebase Realtime Database and navigates
     * to the appropriate home screen (Buyer or Seller).
     * @param user The FirebaseUser object for the logged-in user.
     */
    private void navigateToHome(FirebaseUser user) {
        if (user == null) {
            Log.e(TAG, "navigateToHome called with null user.");
            // Ensure UI is enabled if called unexpectedly
            progressBarLogin.setVisibility(View.GONE);
            buttonLogin.setEnabled(true);
            textViewGoToRegister.setEnabled(true);
            return;
        }
        String uid = user.getUid();
        // Ensure progress bar is visible while fetching role
        progressBarLogin.setVisibility(View.VISIBLE);
        buttonLogin.setEnabled(false);
        textViewGoToRegister.setEnabled(false);

        DatabaseReference userRef = mDatabaseUsersRef.child(uid); // Use the class member mDatabaseUsersRef

        userRef.child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Hide progress bar once data is fetched or fails
                progressBarLogin.setVisibility(View.GONE);

                if (snapshot.exists()) {
                    String role = snapshot.getValue(String.class);
                    Log.d(TAG, "User role fetched: " + role);
                    Intent intent;
                    if ("Seller".equalsIgnoreCase(role)) {
                        intent = new Intent(LoginActivity.this, SellerHomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish(); // Finish LoginActivity
                    } else if ("Buyer".equalsIgnoreCase(role)) {
                        // TODO: Create BuyerHomeActivity first
                         intent = new Intent(LoginActivity.this, BuyerHomeActivity.class);
                         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                         startActivity(intent);
                         finish(); // Finish LoginActivity
                        Toast.makeText(LoginActivity.this, "Buyer Login Successful )", Toast.LENGTH_SHORT).show();
                        // TEMPORARILY enable buttons again if BuyerHome doesn't exist and sign out

                    }
                    else {
                        // Role not found or unexpected value
                        Log.w(TAG, "User role is null or unexpected: " + role);
                        Toast.makeText(LoginActivity.this, "Could not determine user role.", Toast.LENGTH_SHORT).show();
                        // Re-enable buttons so user can try again or register
                        buttonLogin.setEnabled(true);
                        textViewGoToRegister.setEnabled(true);
                        mAuth.signOut(); // Sign out if role is invalid
                    }
                } else {
                    // User exists in Auth, but no data in Database (or role field missing)
                    Log.w(TAG, "User data or role not found in Database for UID: " + uid);
                    Toast.makeText(LoginActivity.this, "User profile data not found. Please register again or contact support.", Toast.LENGTH_LONG).show();
                    // Re-enable buttons
                    buttonLogin.setEnabled(true);
                    textViewGoToRegister.setEnabled(true);
                    mAuth.signOut(); // Sign out the user as their profile is incomplete
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read user role.", error.toException());
                progressBarLogin.setVisibility(View.GONE);
                buttonLogin.setEnabled(true);
                textViewGoToRegister.setEnabled(true);
                Toast.makeText(LoginActivity.this, "DB Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                mAuth.signOut(); // Sign out on DB error? Optional.
            }
        });
    }
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();




}
