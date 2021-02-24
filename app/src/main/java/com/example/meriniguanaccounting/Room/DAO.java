package com.example.meriniguanaccounting.Room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DAO {

    @Insert
    long addItem(Item item);

    @Update
    int updateItem(Item item);

    @Delete
    int deleteItem(Item item);

    @Query("DELETE FROM " + RoomUtils.TABLE_NAME)
    int deleteAll();

    @Query("SELECT * FROM " + RoomUtils.TABLE_NAME)
    List<Item> getAllItems();

    @Query("SELECT * FROM " + RoomUtils.TABLE_NAME +
            " WHERE " + RoomUtils.COLUMN_INFO_ID+ " ==:itemId")
    Item getItem(long itemId);

}
