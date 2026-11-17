package com.example.safesmstest;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {SmsEntity.class}, version = 1, exportSchema = false)
public abstract class SmsDatabase extends RoomDatabase {

    private static volatile SmsDatabase INSTANCE;

    public abstract SmsDao smsDao();

    public static SmsDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (SmsDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            SmsDatabase.class, "sms_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
