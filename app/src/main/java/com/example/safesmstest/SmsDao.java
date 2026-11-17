package com.example.safesmstest;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SmsDao {
    @Insert
    void insertSms(SmsEntity sms);

    @Query("SELECT * FROM pending_sms ORDER BY timestamp ASC")
    List<SmsEntity> getAllPendingSms();

    @Delete
    void deleteSms(SmsEntity sms);
}
