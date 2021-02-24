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

public class EditItemDialog extends DialogFragment {

    public interface EditItemDialogListener {
        void onEditItemDialogPositiveClick(
                DialogFragment dialog,  Item itemToEdit, int oldDifference, int newDifference);
        void onEditItemDialogNegativeClick(
                DialogFragment dialog, Item itemToDelete, int differenceOfDeletedItem);
    }

    EditItemDialogListener listener;

    AccountingDatabase database;

    EditText dateEditText, plusOrMinusEditText,
            differenceEditText, reasonEditText;

    AlertDialog editItemDialog;
    Context context;

    Item itemToEdit;

    int oldDifference, newDifference;

    int rowsUpdated, rowsDeleted;

    int timesDeleteButtonWasTapped = 0;

    public EditItemDialog(Item itemToEdit) {
        this.itemToEdit = itemToEdit;
        this.oldDifference = itemToEdit.getDifference();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        context = getContext();
        View view = inflater.inflate(R.layout.dialog_edit_item, null);

        builder.setTitle(getString(R.string.edit_item));
        builder.setView(view);

        initDatabase();
        initEditTextFields(view);
        setTexts();

        builder.setPositiveButton(getString(R.string.edit), null);
        builder.setNegativeButton(getString(R.string.delete), null);
        builder.setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        editItemDialog = builder.create();

        editItemDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveButton = editItemDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editItem();
                    }
                });

                Button negativeButton = editItemDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        timesDeleteButtonWasTapped++;
                        if (timesDeleteButtonWasTapped == 1) {
                            Toast.makeText(context, R.string.tap_delete_button_again_to_delete_item,
                                    Toast.LENGTH_LONG).show();
                        } else if (timesDeleteButtonWasTapped == 2) {
                            deleteItem();
                            dismiss();
                        }
                    }
                });
            }
        });

        return editItemDialog;
    }

    public void initDatabase() {
        database = Room.databaseBuilder(getActivity().getApplicationContext(),
                AccountingDatabase.class, RoomUtils.DATABASE_NAME).build();
    }

    private void initEditTextFields(View view) {
        dateEditText = view.findViewById(R.id.dateEditText);
        plusOrMinusEditText = view.findViewById(R.id.plusOrMinusEditText);
        differenceEditText = view.findViewById(R.id.differenceEditText);
        reasonEditText = view.findViewById(R.id.reasonEditText);
    }


    private void setTexts() {
        dateEditText.setText(itemToEdit.getDate());
        if (itemToEdit.isIncome()) {
            plusOrMinusEditText.setText("+");
        } else {
            plusOrMinusEditText.setText("-");
        }
        differenceEditText.setText(removeMinusAtStartIfItIs(String.valueOf(itemToEdit.getDifference())));
        reasonEditText.setText(itemToEdit.getReason());
    }


    private void editItem() {
        if (allEditTextFieldsAreNotEmpty()
                && plusOrMinusEditTextHasAppropriateInput()) {
            if (isIncome()) {
                newDifference = +Integer.parseInt(differenceEditText.getText().toString().trim());
            } else {
                newDifference = -Integer.parseInt(differenceEditText.getText().toString().trim());
            }
            String date = dateEditText.getText().toString().trim();
            String reason = reasonEditText.getText().toString().trim();

            int moneyAmount = itemToEdit.getMoneyAmount() - oldDifference + newDifference;

            itemToEdit.setDate(date);
            itemToEdit.setIncome(isIncome());
            itemToEdit.setDifference(newDifference);
            itemToEdit.setReason(reason);
            itemToEdit.setMoneyAmount(moneyAmount);

            updateMoneyAmountInShredPreferences();
            new EditItemAsyncTask().execute();
            dismiss();
        }
    }

    private void deleteItem() {
        newDifference = 0;
        updateMoneyAmountInShredPreferences();
        new DeleteItemAsyncTask().execute();
    }


    private boolean allEditTextFieldsAreNotEmpty() {
        if (TextUtils.isEmpty(dateEditText.getText().toString().trim())) {
            Toast.makeText(context,
                    R.string.enter_a_date, Toast.LENGTH_LONG).show();
            return false;
        } else if (TextUtils.isEmpty(plusOrMinusEditText.getText().toString().trim())) {
            Toast.makeText(context,
                    R.string.enter_plus_or_minus, Toast.LENGTH_LONG).show();
            return false;
        } else if (TextUtils.isEmpty(differenceEditText.getText().toString().trim())) {
            Toast.makeText(context,
                    R.string.enter_a_difference, Toast.LENGTH_LONG).show();
            return false;
        } else if (TextUtils.isEmpty(reasonEditText.getText().toString().trim())) {
            Toast.makeText(context,
                    R.string.enter_reason, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private boolean plusOrMinusEditTextHasAppropriateInput() {
        return plusOrMinusEditText.getText().toString().trim().equals("+")
                || plusOrMinusEditText.getText().toString().trim().equals("-");
    }

    private boolean isIncome() {
        if (plusOrMinusEditText.getText().toString().trim().equals("+")) {
            return true;
        } else {
            Toast.makeText(context, "Enter + or -", Toast.LENGTH_LONG).show();
            return false;
        }
    }


    private void updateMoneyAmountInShredPreferences() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
                Util.sharedPreferencesName, Context.MODE_PRIVATE);
        int oldMoneyAmount = sharedPreferences.getInt(Util.moneyAmountKey, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Util.moneyAmountKey, oldMoneyAmount - oldDifference + newDifference);
        editor.apply();
    }


    private void notifyAboutResultOfEditing() {
        if (rowsUpdated != 0) {
            Toast.makeText(context,
                    R.string.data_saved, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context,
                    R.string.editing_of_data_in_table_failed, Toast.LENGTH_LONG).show();
        }
    }

    private void notifyAboutResultOfDeleting() {
        if (rowsDeleted != 0) {
            Toast.makeText(context,
                    R.string.item_is_deleted, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context,
                    R.string.deleting_of_data_from_table_failed, Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (EditItemDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Must implement EditItemDialogListener");
        }
    }


    private String removeMinusAtStartIfItIs(String string) {
        if (string.charAt(0) == '-') {
            return string.substring(1);
        }
        return string;
    }


    private class EditItemAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            rowsUpdated = database.getDAO().updateItem(itemToEdit);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            notifyAboutResultOfEditing();
            listener.onEditItemDialogPositiveClick(
                    EditItemDialog.this, itemToEdit, oldDifference, newDifference);
        }
    }

    private class DeleteItemAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            rowsDeleted = database.getDAO().deleteItem(itemToEdit);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            notifyAboutResultOfDeleting();
            listener.onEditItemDialogNegativeClick(
                    EditItemDialog.this, itemToEdit, oldDifference);
        }
    }
}
