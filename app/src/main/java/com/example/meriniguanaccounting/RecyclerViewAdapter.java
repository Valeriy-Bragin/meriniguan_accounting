package com.example.meriniguanaccounting;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meriniguanaccounting.Room.Item;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewViewHolder> {

    private MainActivity mainActivity;
    private ArrayList<Item> itemArrayList;

    public RecyclerViewAdapter(ArrayList<Item> itemArrayList, MainActivity mainActivity) {
        this.itemArrayList = itemArrayList;
        this.mainActivity = mainActivity;
    }


    public static class RecyclerViewViewHolder extends RecyclerView.ViewHolder {

        TextView dateTextView, plusOrMinusTextView, differenceTextView,
                reasonTextView, moneyAmountTextView;

        public RecyclerViewViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            plusOrMinusTextView = itemView.findViewById(R.id.plusOrMinusTextView);
            differenceTextView = itemView.findViewById(R.id.differenceTextView);
            reasonTextView = itemView.findViewById(R.id.reasonTextView);
            moneyAmountTextView = itemView.findViewById(R.id.moneyAmountTextView);
        }
    }

    @NonNull
    @Override
    public RecyclerViewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false);
        RecyclerViewViewHolder holder = new RecyclerViewViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewViewHolder holder, int position) {
        final Item currentItem = itemArrayList.get(position);

        holder.dateTextView.setText(currentItem.getDate());
        holder.differenceTextView.setText(
                removeMinusAtStartIfItIs(String.valueOf(currentItem.getDifference())));
        holder.reasonTextView.setText(currentItem.getReason());
        holder.moneyAmountTextView.setText(String.valueOf(currentItem.getMoneyAmount()));

        if (currentItem.isIncome()) {
            holder.plusOrMinusTextView.setText("+");
            holder.itemView.setBackgroundResource(R.color.colorIncome);
        } else {
            holder.plusOrMinusTextView.setText("-");
            holder.itemView.setBackgroundResource(R.color.colorExpense);
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mainActivity.showEditItemDialog(currentItem);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemArrayList.size();
    }

    private String removeMinusAtStartIfItIs(String string) {
        if (string.charAt(0) == '-') {
            return string.substring(1);
        }
        return string;
    }
}
