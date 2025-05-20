package com.example.legacy_order.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.legacy_order.R;
import com.example.legacy_order.models.Order;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class BuyerOrderAdapter extends RecyclerView.Adapter<BuyerOrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orders;

    public BuyerOrderAdapter(Context context, List<Order> orders) {
        this.context = context;
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_buyer, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);

        // Set order details
        holder.textProduct.setText(order.getProductName());
        holder.textPrice.setText(String.format("Price: DA%.2f", order.getPrice()));
        holder.textLocation.setText(String.format("Pickup: %s", order.getPickupLocation()));

        // Format date and time
        String dateTime = String.format("%s at %s", order.getPickupDate(), order.getPickupTime());
        holder.textDateTime.setText(dateTime);

        // Set status
        holder.textStatus.setText(String.format("Status: %s", order.getStatus()));

        // Make location clickable for editing
        holder.textLocation.setOnClickListener(v -> {
            if (canEditLocation(order)) {
                showLocationEditDialog(order);
            } else {
                Toast.makeText(context,
                        "Location can only be changed within 10 minutes of order creation",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    private boolean canEditLocation(Order order) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date orderTime = sdf.parse(order.getPickupDate() + " " + order.getPickupTime());
            Date currentTime = new Date();

            long diffInMillis = currentTime.getTime() - (orderTime != null ? orderTime.getTime() : 0);
            long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);

            return diffInMinutes <= 10 && !order.getStatus().equals("Delivered");
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showLocationEditDialog(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Change Pickup Location");

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(order.getPickupLocation());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newLocation = input.getText().toString().trim();
            if (!newLocation.isEmpty()) {
                updateOrderLocation(order.getOrderId(), newLocation);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void updateOrderLocation(String orderId, String newLocation) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance()
                .getReference("Orders")
                .child(orderId);

        orderRef.child("pickupLocation").setValue(newLocation)
                .addOnSuccessListener(aVoid -> Toast.makeText(context,
                        "Location updated successfully",
                        Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context,
                        "Failed to update location: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView textProduct, textPrice, textLocation, textDateTime, textStatus;

        OrderViewHolder(View itemView) {

            super(itemView);
            textProduct = itemView.findViewById(R.id.textViewOrderProduct);
            textPrice = itemView.findViewById(R.id.textViewOrderPrice);
            textLocation = itemView.findViewById(R.id.textViewOrderLocation);
            textDateTime = itemView.findViewById(R.id.textViewOrderDateTime);
            textStatus = itemView.findViewById(R.id.textViewOrderStatus);
        }
    }
}