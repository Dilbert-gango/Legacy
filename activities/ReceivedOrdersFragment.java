package com.example.legacy_order.activities;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.legacy_order.R;
import com.example.legacy_order.adapters.BuyerOrderAdapter; // Your existing adapter
import com.example.legacy_order.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ReceivedOrdersFragment extends Fragment {

    private RecyclerView recyclerView;
    private BuyerOrderAdapter orderAdapter;
    private List<Order> orderList;
    private ProgressBar progressBar;
    private TextView emptyText;
    private DatabaseReference ordersRef;
    private FirebaseAuth mAuth;

    public ReceivedOrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewOrdersFragment);
        progressBar = view.findViewById(R.id.progressBarOrdersFragment);
        emptyText = view.findViewById(R.id.textViewEmptyOrdersFragment);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        orderList = new ArrayList<>();
        orderAdapter = new BuyerOrderAdapter(getContext(), orderList); // Pass context
        recyclerView.setAdapter(orderAdapter);

        mAuth = FirebaseAuth.getInstance();
        ordersRef = FirebaseDatabase.getInstance().getReference("Orders");

        loadBuyerOrders();
    }

    private void loadBuyerOrders() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Not logged in.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            emptyText.setText("Please login to see your orders.");
            emptyText.setVisibility(View.VISIBLE);
            return;
        }
        String currentBuyerId = currentUser.getUid();
        progressBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        ordersRef.orderByChild("buyerId").equalTo(currentBuyerId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderList.clear();
                        for (DataSnapshot orderSnap : snapshot.getChildren()) {
                            Order order = orderSnap.getValue(Order.class);
                            if (order != null) {
                                order.setOrderId(orderSnap.getKey()); // Important
                                // Add creationTimestamp if you implemented it in Order model
                                // if (orderSnap.child("creationTimestamp").exists()){
                                //    order.setCreationTimestamp(orderSnap.child("creationTimestamp").getValue(Long.class));
                                // }
                                orderList.add(order);
                            }
                        }

                        progressBar.setVisibility(View.GONE);
                        if (orderList.isEmpty()) {
                            emptyText.setText("You have no received orders.");
                            emptyText.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            emptyText.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            orderAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Log.e("ReceivedOrdersFragment", "Database error: " + error.getMessage());
                        Toast.makeText(getContext(), "Failed to load orders.", Toast.LENGTH_SHORT).show();
                        emptyText.setText("Failed to load orders.");
                        emptyText.setVisibility(View.VISIBLE);
                    }
                });
    }
}