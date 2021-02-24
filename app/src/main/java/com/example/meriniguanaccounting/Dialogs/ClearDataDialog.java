package com.example.meriniguanaccounting.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.room.Room;

import com.example.meriniguanaccounting.R;
import com.example.meriniguanaccounting.Room.AccountingDatabase;
import com.example.meriniguanaccounting.Room.RoomUtils;

public class ClearDataDialog extends DialogFragment {

    public interface ClearDataDialogListener {
        void onClearDataDialogPositiveClick();
    }

    ClearDataDialogListener listener;

    AccountingDatabase database;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        initDatabase();

        builder.setMessage(getString(R.string.do_you_really_want_to_clear_data));

        builder.setPositiveButton(getString(R.string.clear), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onClearDataDialogPositiveClick();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        return builder.create();
    }

    private void initDatabase() {
        database = Room.databaseBuilder(getContext(),
                AccountingDatabase.class, RoomUtils.DATABASE_NAME).build();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (ClearDataDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " Must implement ClearDataDialogListener");
        }
    }
}
