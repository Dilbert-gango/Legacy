package com.example.legacy_order.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.legacy_order.R;
import com.example.legacy_order.models.Order;

import java.util.List;

public class SellerOrderAdapter extends RecyclerView.Adapter<SellerOrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;

    public SellerOrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.textProduct.setText(order.getProductName());
        holder.textBuyer.setText(order.getBuyerId());
        holder.textPrice.setText("DA" + order.getPrice());
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView textProduct, textBuyer, textPrice;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textProduct = itemView.findViewById(R.id.textProductName);
            textBuyer = itemView.findViewById(R.id.textBuyerName);
            textPrice = itemView.findViewById(R.id.textOrderPrice);
        }
    }
}
