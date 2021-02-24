package com.example.meriniguanaccounting;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.meriniguanaccounting.Dialogs.ClearDataDialog;
import com.example.meriniguanaccounting.Room.AccountingDatabase;
import com.example.meriniguanaccounting.Room.RoomUtils;
import com.example.meriniguanaccounting.Utils.Util;

public class SettingsActivity extends AppCompatActivity
    implements ClearDataDialog.ClearDataDialogListener {

    AccountingDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        database = Room.databaseBuilder(getApplicationContext(),
                AccountingDatabase.class, RoomUtils.DATABASE_NAME).build();
    }

    public void showClearDataDialog(View view) {
        ClearDataDialog clearDataDialog = new ClearDataDialog();
        clearDataDialog.show(getSupportFragmentManager(), "clearData");
    }

    public void clearData() {
        new DeleteDataFromAccountingDatabaseAsyncTask().execute();
        resetMoneyAmountInSharedPreferences();
        Util.dataCleared = true;
    }

    private void resetMoneyAmountInSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(
                Util.sharedPreferencesName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Util.moneyAmountKey, 0);
        editor.apply();
    }

    private void notifyAboutResultOfClearingData(int rowsDeleted) {
        if (rowsDeleted != 0) {
            Toast.makeText(getApplicationContext(),
                    "Data cleared", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClearDataDialogPositiveClick() {
        clearData();
    }

    private class DeleteDataFromAccountingDatabaseAsyncTask extends AsyncTask<Void, Void, Void> {

        int rowsDeleted;

        @Override
        protected Void doInBackground(Void... voids) {
            rowsDeleted = database.getDAO().deleteAll();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            notifyAboutResultOfClearingData(rowsDeleted);
        }
    }
}