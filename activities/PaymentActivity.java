package com.example.legacy_order.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.legacy_order.R;
import com.example.legacy_order.models.Order;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

// Removed Stripe imports

public class PaymentActivity extends AppCompatActivity {

    private Order currentOrder;
    private TextView textViewPaymentInfo;
    private Button buttonConfirmPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get order from intent
        currentOrder = (Order) getIntent().getSerializableExtra("ORDER");

        if (currentOrder == null) {
            Toast.makeText(this, "Error: Order details not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }


        // Display some order info for confirmation
        String paymentDetails = String.format("Product: %s\nPrice: DA%.2f",
                currentOrder.getProductName(), currentOrder.getPrice());
        textViewPaymentInfo.setText(paymentDetails);

        buttonConfirmPayment.setOnClickListener(v -> {
            // Simulate payment
            simulatePayment();
        });
    }

    private void simulatePayment() {
        // Here, you just update the order status and pretend payment was successful
        Toast.makeText(this, "Simulating payment...", Toast.LENGTH_SHORT).show();

        // Update order status to "Paid" (or "Processing" if you have such a state)
        updateOrderStatus(currentOrder.getOrderId(), "Paid");

        // Show success message
        Toast.makeText(this, "Payment Simulated Successfully for Order ID: " + currentOrder.getOrderId(), Toast.LENGTH_LONG).show();

        // Optionally, navigate back or close
        finish();
    }

    private void updateOrderStatus(String orderId, String status) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance()
                .getReference("Orders")
                .child(orderId);

        orderRef.child("status").setValue(status)
                .addOnSuccessListener(aVoid -> {
                    Log.d("PaymentActivity", "Order status updated to " + status);
                    // Don't show toast here if you show one in simulatePayment already
                })
                .addOnFailureListener(e -> {
                    Log.e("PaymentActivity", "Failed to update order status", e);
                    Toast.makeText(this, "Failed to update order status in database.", Toast.LENGTH_SHORT).show();
                });
    }
}