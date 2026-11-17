package com.example.safesmstest;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pending_sms")
public class SmsEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String phoneNumber;
    private String message;
    private long timestamp;

    public SmsEntity(String phoneNumber, String message, long timestamp) {
        this.phoneNumber = phoneNumber;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPhoneNumber() { return phoneNumber; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
}
