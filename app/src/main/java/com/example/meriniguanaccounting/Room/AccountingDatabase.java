package com.example.meriniguanaccounting.Room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(version = RoomUtils.DATABASE_VERSION, entities = {Item.class})
public abstract class AccountingDatabase extends RoomDatabase {

    public abstract DAO getDAO();

}
