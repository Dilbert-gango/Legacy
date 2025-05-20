// BuyerHomeActivity.java
package com.example.legacy_order.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
// Remove unused imports like ProgressBar, TextView, RecyclerView related ones if they are no longer direct members
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2; // Import ViewPager2

import com.example.legacy_order.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

public class BuyerHomeActivity extends AppCompatActivity {

    // Remove:
    // private RecyclerView recyclerView;
    // private BuyerOrderAdapter orderAdapter;
    // private List<Order> orderList;
    // private ProgressBar progressBar;
    // private TextView emptyText;
    // private DatabaseReference ordersRef;

    private FirebaseAuth mAuth;
    private Toolbar toolbar;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private BuyerPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_home); // This layout needs to be updated

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Orders");
        }

        mAuth = FirebaseAuth.getInstance();

        viewPager = findViewById(R.id.viewPagerBuyer);
        tabLayout = findViewById(R.id.tabLayoutBuyer);

        pagerAdapter = new BuyerPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("All My Orders");
                    break;
                case 1:
                    tab.setText("Paid Orders");
                    break;
            }
        }).attach();

        // loadBuyerOrders(); // This logic is now in the fragments
    }

    // loadBuyerOrders() method is removed as its logic is moved to ReceivedOrdersFragment

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
            Intent intent = new Intent(BuyerHomeActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}