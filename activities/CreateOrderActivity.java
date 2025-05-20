package com.example.legacy_order.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.legacy_order.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateOrderActivity extends AppCompatActivity {


    private static final String TAG = "CreateOrderActivity";

    // UI Elements
    private EditText editTextProductName, editTextPrice, editTextPickupDate, editTextPickupTime, editTextPickupLocation, editTextBuyerId;
    private Button buttonCreateOrder;
    private ProgressBar progressBarCreateOrder;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseOrdersRef; // Reference to "Orders" node
    private FirebaseUser currentUser;

    // Calendar for Date/Time Pickers
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);

        // Initialize Firebase Auth & Database Reference
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mDatabaseOrdersRef = FirebaseDatabase.getInstance().getReference("Orders"); // Point to the "Orders" node

        // Redirect to Login if user is null (safety check)
        if (currentUser == null) {
            Toast.makeText(this, "Error: Not logged in.", Toast.LENGTH_SHORT).show();
            // Optionally redirect to Login
            // Intent intent = new Intent(CreateOrderActivity.this, LoginActivity.class);
            // startActivity(intent);
            finish(); // Close this activity
            return;
        }

        // Initialize UI Elements
        editTextProductName = findViewById(R.id.editTextProductName);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextPickupDate = findViewById(R.id.editTextPickupDate);
        editTextPickupTime = findViewById(R.id.editTextPickupTime);
        editTextPickupLocation = findViewById(R.id.editTextPickupLocation);
        editTextBuyerId = findViewById(R.id.editTextBuyerId);
        buttonCreateOrder = findViewById(R.id.buttonCreateOrder);
        progressBarCreateOrder = findViewById(R.id.progressBarCreateOrder);

        // Initialize Calendar instance
        calendar = Calendar.getInstance();

        // --- Set up Date Picker ---
        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateLabel();
            }
        };

        editTextPickupDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(CreateOrderActivity.this, dateSetListener,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // --- Set up Time Picker ---
        final TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                updateTimeLabel();
            }
        };

        editTextPickupTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(CreateOrderActivity.this, timeSetListener,
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                        true).show(); // true for 24-hour format
            }
        });


        // --- Set up Create Order Button ---
        buttonCreateOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitOrder();
            }
        });

        // Add Back Button to Action Bar (Optional)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Create New Order"); // Set title
        }
    }

    // Handle Back button press in Action Bar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Default behavior (go back)
        return true;
    }


    private void updateDateLabel() {
        String myFormat = "yyyy-MM-dd"; // Choose your desired date format
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        editTextPickupDate.setText(sdf.format(calendar.getTime()));
    }

    private void updateTimeLabel() {
        String myFormat = "HH:mm"; // Choose your desired time format (24-hour)
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        editTextPickupTime.setText(sdf.format(calendar.getTime()));
    }


    private void submitOrder() {
        // Get input values
        String productName = editTextProductName.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();
        String pickupDate = editTextPickupDate.getText().toString().trim();
        String pickupTime = editTextPickupTime.getText().toString().trim();
        String pickupLocation = editTextPickupLocation.getText().toString().trim();
        String buyerId = editTextBuyerId.getText().toString().trim();
        String sellerId = currentUser.getUid(); // Get the current seller's ID

        // --- Input Validation ---
        if (TextUtils.isEmpty(productName)) {
            editTextProductName.setError("Product name is required.");
            editTextProductName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(priceStr)) {
            editTextPrice.setError("Price is required.");
            editTextPrice.requestFocus();
            return;
        }
        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            editTextPrice.setError("Invalid price format.");
            editTextPrice.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(pickupDate)) {
            // Technically handled by picker, but good practice
            Toast.makeText(this, "Pickup date is required.", Toast.LENGTH_SHORT).show();
            editTextPickupDate.requestFocus(); // Or trigger picker again
            return;
        }
        if (TextUtils.isEmpty(pickupTime)) {
            Toast.makeText(this, "Pickup time is required.", Toast.LENGTH_SHORT).show();
            editTextPickupTime.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(pickupLocation)) {
            editTextPickupLocation.setError("Pickup location is required.");
            editTextPickupLocation.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(buyerId)) {
            editTextBuyerId.setError("Buyer ID is required.");
            editTextBuyerId.requestFocus();
            return;
        }
        // TODO: Optional - Add validation to check if the buyerId actually exists in the Users node.
        // This would require another Firebase query before saving the order.

        // --- End Input Validation ---

        // Show progress bar, disable button
        progressBarCreateOrder.setVisibility(View.VISIBLE);
        buttonCreateOrder.setEnabled(false);

        // --- Prepare Order Data ---
        // Generate a unique key for the new order using push()
        String orderId = mDatabaseOrdersRef.push().getKey();

        if (orderId == null) {
            Log.e(TAG, "Couldn't get push key for orders");
            Toast.makeText(this, "Error creating order ID.", Toast.LENGTH_SHORT).show();
            progressBarCreateOrder.setVisibility(View.GONE);
            buttonCreateOrder.setEnabled(true);
            return;
        }

        // Create a Map to hold the order data
        // Later, we'll replace this with an Order class/object
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", orderId); // Store the ID within the order data itself too
        orderData.put("productName", productName);
        orderData.put("price", price); // Store as double
        orderData.put("pickupDate", pickupDate);
        orderData.put("pickupTime", pickupTime);
        orderData.put("pickupLocation", pickupLocation);
        orderData.put("buyerId", buyerId);
        orderData.put("sellerId", sellerId);
        orderData.put("status", "Pending"); // Initial status
        orderData.put("creationTimestamp", System.currentTimeMillis()); // Optional: timestamp
        FirebaseDatabase.getInstance()

                //remove this if needed
                .getReference("BuyersOrders")
                .child(buyerId)
                .child(orderId)
                .setValue(orderData);
        // --- Save Order to Firebase Realtime Database ---
        mDatabaseOrdersRef.child(orderId).setValue(orderData)

                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Order created successfully with ID: " + orderId);
                        progressBarCreateOrder.setVisibility(View.GONE);
                        // buttonCreateOrder is already disabled, no need to re-enable yet

                        Toast.makeText(CreateOrderActivity.this, "Order created successfully!", Toast.LENGTH_SHORT).show();
                        // Finish this activity and return to the SellerHomeActivity
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error creating order", e);
                        progressBarCreateOrder.setVisibility(View.GONE);
                        buttonCreateOrder.setEnabled(true); // Re-enable button on failure
                        Toast.makeText(CreateOrderActivity.this, "Failed to create order: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }}