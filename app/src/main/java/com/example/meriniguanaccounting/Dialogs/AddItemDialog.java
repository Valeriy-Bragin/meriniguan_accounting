package com.example.meriniguanaccounting.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.room.Room;

import com.example.meriniguanaccounting.R;
import com.example.meriniguanaccounting.Room.AccountingDatabase;
import com.example.meriniguanaccounting.Room.Item;
import com.example.meriniguanaccounting.Room.RoomUtils;
import com.example.meriniguanaccounting.Utils.Util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AddItemDialog extends DialogFragment {

    AccountingDatabase database;

    Context context;

    TextView dateValueTextView, plusOrMinusTextView;
    EditText differenceEditText, reasonEditText;

    AlertDialog addItemDialog;
    Item itemToAdd;
    long idOfAddedItem;

    boolean isIncome;

    public interface AddItemDialogListener {
        void onAddItemDialogPositiveClick(DialogFragment dialog, Item addedItem);
    }

    AddItemDialogListener listener;

    public AddItemDialog(boolean isIncome) {
        this.isIncome = isIncome;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        context = getContext();

        View view = inflater.inflate(R.layout.dialog_add_item, null);
        TextView plusOrMinusTextView = view.findViewById(R.id.plusOrMinusTextView);
        plusOrMinusTextView.setText("+");

        if (isIncome) {
            builder.setTitle(getString(R.string.income));
        } else {
            builder.setTitle(getString(R.string.expense));
        }
        builder.setView(view);

        initDatabase();
        initViewFields(view);
        setTextViewTexts();

        builder.setPositiveButton(R.string.add, null);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        addItemDialog = builder.create();

        addItemDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveButton = addItemDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (allEditTextFieldsAreNotEmpty()) {
                            int difference;
                            if (isIncome) {
                                difference = +Integer.parseInt(differenceEditText.getText().toString().trim());
                            } else {
                                difference = -Integer.parseInt(differenceEditText.getText().toString().trim());
                            }
                            String date = dateValueTextView.getText().toString();
                            String reason = reasonEditText.getText().toString().trim();
                            int moneyAmount = getMoneyAmountFromSharedPreferences() + difference;

                            updateMoneyAmountInSharedPreferences(moneyAmount);

                            itemToAdd = new Item(isIncome, date, difference, reason, moneyAmount);

                            new AddItemAsyncTask().execute();
                        }
                    }
                });
            }
        });

        return addItemDialog;
    }


    public void initDatabase() {
        database = Room.databaseBuilder(getActivity(),
                AccountingDatabase.class, RoomUtils.DATABASE_NAME).build();
    }

    public void initViewFields(View view) {
        dateValueTextView = view.findViewById(R.id.dateValueTextView);
        plusOrMinusTextView = view.findViewById(R.id.plusOrMinusTextView);
        differenceEditText = view.findViewById(R.id.differenceEditText);
        reasonEditText = view.findViewById(R.id.reasonEditText);
    }


    private void setTextViewTexts() {
        dateValueTextView.setText(getCurrentDate());
        if (isIncome) {
            plusOrMinusTextView.setText("+");
        } else {
            plusOrMinusTextView.setText("-");
        }
    }


    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (AddItemDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Must implement AddIncomeItemDialogListener");
        }
    }


    private String getCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Date now = new Date();
        return formatter.format(now);
    }


    private boolean allEditTextFieldsAreNotEmpty() {
        if (TextUtils.isEmpty(differenceEditText.getText().toString().trim())) {
            Toast.makeText(context,
                    R.string.enter_a_difference, Toast.LENGTH_LONG).show();
            return false;
        } else if (TextUtils.isEmpty(reasonEditText.getText().toString().trim())) {
            Toast.makeText(context,
                    R.string.enter_a_reason, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


    private int getMoneyAmountFromSharedPreferences() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
                Util.sharedPreferencesName, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(Util.moneyAmountKey, 0);
    }

    private void updateMoneyAmountInSharedPreferences(int newMoneyAmount) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
                Util.sharedPreferencesName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Util.moneyAmountKey, newMoneyAmount);
        editor.apply();
    }


    private void notifyAboutResultOfAddition() {
        if (idOfAddedItem != -1) {
            Toast.makeText(context,
                    R.string.data_saved, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context,
                    R.string.insertion_of_data_in_the_table_failed,
                    Toast.LENGTH_LONG).show();
        }
    }


    private class AddItemAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            idOfAddedItem = database.getDAO().addItem(itemToAdd);
            itemToAdd.setId(idOfAddedItem);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            listener.onAddItemDialogPositiveClick(
                    AddItemDialog.this, itemToAdd);
            notifyAboutResultOfAddition();
            addItemDialog.dismiss();
        }
    }
}
