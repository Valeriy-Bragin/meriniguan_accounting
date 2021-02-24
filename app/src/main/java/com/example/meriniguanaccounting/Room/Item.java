package com.example.meriniguanaccounting.Room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = RoomUtils.TABLE_NAME)
public class Item {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = RoomUtils.COLUMN_INFO_ID)
    private long id;

    @ColumnInfo(name = RoomUtils.COLUMN_INFO_IS_INCOME)
    private boolean isIncome;

    @ColumnInfo(name = RoomUtils.COLUMN_INFO_DATE)
    private String date;

    @ColumnInfo(name = RoomUtils.COLUMN_INFO_DIFFERENCE)
    private int difference;

    @ColumnInfo(name = RoomUtils.COLUMN_INFO_REASON)
    private String reason;

    @ColumnInfo(name = RoomUtils.COLUMN_INFO_MONEY_AMOUNT)
    private int moneyAmount;


    public Item(boolean isIncome, String date, int difference, String reason, int moneyAmount) {
        this.isIncome = isIncome;
        this.date = date;
        this.difference = difference;
        this.reason = reason;
        this.moneyAmount = moneyAmount;
    }


    public long getId() {
        return id;
    }

    public boolean isIncome() {
        return isIncome;
    }

    public String getDate() {
        return date;
    }

    public int getDifference() {
        return difference;
    }

    public String getReason() {
        return reason;
    }

    public int getMoneyAmount() {
        return moneyAmount;
    }


    public void setId(long id) {
        this.id = id;
    }

    public void setIncome(boolean income) {
        isIncome = income;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDifference(int difference) {
        this.difference = difference;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setMoneyAmount(int moneyAmount) {
        this.moneyAmount = moneyAmount;
    }

    @NonNull
    @Override
    public String toString() {
        return this.getDifference() + " " + this.getReason();
    }
}
