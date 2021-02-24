package com.example.meriniguanaccounting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.meriniguanaccounting.Dialogs.AddItemDialog;
import com.example.meriniguanaccounting.Dialogs.EditItemDialog;
import com.example.meriniguanaccounting.Room.AccountingDatabase;
import com.example.meriniguanaccounting.Room.Item;
import com.example.meriniguanaccounting.Room.RoomUtils;
import com.example.meriniguanaccounting.Utils.Util;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
    implements SharedPreferences.OnSharedPreferenceChangeListener,
        AddItemDialog.AddItemDialogListener, EditItemDialog.EditItemDialogListener {

    AccountingDatabase database;

    RecyclerView recyclerView;
    ArrayList<Item> recyclerViewItemArrayList = new ArrayList<>();
    RecyclerViewAdapter recyclerViewAdapter;

    Item itemToEditWithOldState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setThemeFromSharedPreferences();

        displayData();

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Util.dataCleared) {
            clearData();
            Util.dataCleared = false;
        }
    }

    private void displayData() {
        initDatabase();

        initRecyclerViewItemAdapter();

        initRecyclerViewItemArrayList();

        initRecyclerView();
    }

    public void clearData() {
        recyclerViewItemArrayList.clear();
        recyclerViewAdapter.notifyDataSetChanged();
    }

    private void setThemeFromSharedPreferences() {
        SharedPreferences defaultSharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        switch (defaultSharedPreferences.getString("theme", "Light")) {
            case "Light":
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "Dark":
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "Set by Battery Saver":
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                break;
        }
    }


    private void initDatabase() {
        database = Room.databaseBuilder(getApplicationContext(),
                AccountingDatabase.class, RoomUtils.DATABASE_NAME).build();
    }

    private void initRecyclerViewItemAdapter() {
        recyclerViewAdapter
                = new RecyclerViewAdapter(recyclerViewItemArrayList, this);
    }

    private void initRecyclerViewItemArrayList() {
        new AddAllItemsToRecyclerViewItemArrayListFromDatabaseAsyncTask().execute();
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


    public void handleIncome(View view) {
        AddItemDialog addIncomeItemDialog = new AddItemDialog(true);
        addIncomeItemDialog.show(getSupportFragmentManager(), "addIncomeItem");
    }

    public void handleExpense(View view) {
        AddItemDialog addExpenseItemDialog = new AddItemDialog(false);
        addExpenseItemDialog.show(getSupportFragmentManager(), "addExpenseItem");
    }

    public void showEditItemDialog(Item itemToEdit) {
        itemToEditWithOldState = itemToEdit;
        EditItemDialog editItemDialog = new EditItemDialog(itemToEdit);
        editItemDialog.show(getSupportFragmentManager(), "editItem");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.accounting_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_settings) {
            Intent openSettingsActivity = new Intent(
                    MainActivity.this, SettingsActivity.class);
            startActivity(openSettingsActivity);
        }
        else if (itemId == R.id.action_about) {
            Intent openActivityAbout = new Intent(
                    MainActivity.this, AboutActivity.class);
            startActivity(openActivityAbout);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Util.themeKey)) {
            setThemeFromSharedPreferences();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onAddItemDialogPositiveClick(DialogFragment dialog, Item addedItem) {
        recyclerViewItemArrayList.add(0, addedItem);
        recyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onEditItemDialogPositiveClick(
            DialogFragment dialog, Item itemToEdit, int oldDifference, int newDifference) {

        int indexOfItemToEdit = recyclerViewItemArrayList.indexOf(itemToEditWithOldState);
        recyclerViewItemArrayList.set(indexOfItemToEdit, itemToEdit);

        new UpdateMoneyAmountOfAllItemsStartingSinceItemToEditExcluding(
                itemToEdit.getId(), oldDifference, newDifference).execute();
    }

    @Override
    public void onEditItemDialogNegativeClick(
            DialogFragment dialog, Item itemToDelete, int differenceOfDeletedItem) {

        recyclerViewItemArrayList.remove(itemToDelete);

        new UpdateMoneyAmountOfAllItemsStartingSinceItemToEditExcluding(
                itemToDelete.getId(), differenceOfDeletedItem, 0).execute();
    }


    public ArrayList<Item> reverse(ArrayList<Item> list) {
        for(int i = 0, j = list.size() - 1; i < j; i++) {
            list.add(i, list.remove(j));
        }
        return list;
    }


    private class AddAllItemsToRecyclerViewItemArrayListFromDatabaseAsyncTask
        extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            recyclerViewItemArrayList.clear();
            recyclerViewItemArrayList.addAll(database.getDAO().getAllItems());
            recyclerViewItemArrayList = reverse(recyclerViewItemArrayList);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            recyclerViewAdapter.notifyDataSetChanged();
        }
    }

    private class UpdateMoneyAmountOfAllItemsStartingSinceItemToEditExcluding
        extends AsyncTask<Void, Void, Void> {

        long idOfItemToEdit;
        int oldDifference, newDifference;

        public UpdateMoneyAmountOfAllItemsStartingSinceItemToEditExcluding(
                long idOfItemToEdit, int oldDifference, int newDifference) {
            this.idOfItemToEdit = idOfItemToEdit;
            this.oldDifference = oldDifference;
            this.newDifference = newDifference;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (Item item : recyclerViewItemArrayList) {
                if (item.getId() > idOfItemToEdit) {
                    int indexOfItemInRecyclerViewItemArrayList =
                            recyclerViewItemArrayList.indexOf(item);
                    item.setMoneyAmount(item.getMoneyAmount() - oldDifference + newDifference);
                    recyclerViewItemArrayList.set(indexOfItemInRecyclerViewItemArrayList, item);
                    new UpdateItemAsyncTask().execute(item);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            recyclerViewAdapter.notifyDataSetChanged();
        }
    }

    private class UpdateItemAsyncTask extends AsyncTask<Item, Void, Void> {

        @Override
        protected Void doInBackground(Item... items) {
            database.getDAO().updateItem(items[0]);
            return null;
        }
    }
}