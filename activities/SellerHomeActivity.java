package com.example.legacy_order.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.legacy_order.R;
import com.example.legacy_order.adapters.SellerOrderAdapter;
import com.example.legacy_order.models.Order;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SellerHomeActivity extends AppCompatActivity {

    private static final String TAG = "SellerHomeActivity";

    private FirebaseAuth mAuth;
    private DatabaseReference mOrdersRef;
    private FirebaseUser currentUser;

    private Toolbar toolbar;
    private RecyclerView recyclerViewOrders;
    private FloatingActionButton fabAddOrder;
    private TextView textViewNoOrders;

    private SellerOrderAdapter orderAdapter;
    private List<Order> orderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_home);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Log.w(TAG, "User is null, redirecting to LoginActivity.");
            goToLogin();
            return;
        }

        mOrdersRef = FirebaseDatabase.getInstance().getReference("Orders");

        toolbar = findViewById(R.id.toolbarSellerHome);
        setSupportActionBar(toolbar);

        recyclerViewOrders = findViewById(R.id.recyclerViewSellerOrders);
        fabAddOrder = findViewById(R.id.fabAddOrder);
        textViewNoOrders = findViewById(R.id.textViewNoOrdersSeller);

        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOrders.setHasFixedSize(true);

        orderList = new ArrayList<>();
        orderAdapter = new SellerOrderAdapter(this, orderList);
        recyclerViewOrders.setAdapter(orderAdapter);

        loadSellerOrders();

        fabAddOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SellerHomeActivity.this, CreateOrderActivity.class);
                startActivity(intent);
            }
        });

        Toast.makeText(this, "Seller Home Screen. User: " + currentUser.getEmail(), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            mAuth.signOut();
            goToLogin();
            return true;
        } else if (id == R.id.action_profile) {
            Intent intent = new Intent(SellerHomeActivity.this, ProfileActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadSellerOrders() {
        if (currentUser == null) return;

        String currentSellerId = currentUser.getUid();

        Query sellerOrdersQuery = mOrdersRef.orderByChild("sellerId").equalTo(currentSellerId);

        sellerOrdersQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Order order = ds.getValue(Order.class);
                        if (order != null) {
                            order.setOrderId(ds.getKey());
                            orderList.add(order);
                        }
                    }
                    orderAdapter.notifyDataSetChanged();
                    toggleNoOrdersView(orderList.isEmpty());
                } else {
                    toggleNoOrdersView(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "loadSellerOrders:onCancelled", error.toException());
                Toast.makeText(SellerHomeActivity.this, "Failed to load orders.", Toast.LENGTH_SHORT).show();
                toggleNoOrdersView(true);
            }
        });
    }

    private void toggleNoOrdersView(boolean show) {
        textViewNoOrders.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewOrders.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
