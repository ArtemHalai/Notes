package com.example.notes.data;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StoreProvider {
    private static final StoreProvider instance = new StoreProvider();

    private StoreProvider() {
    }

    public static StoreProvider getInstance() {
        return instance;
    }

    public String getTime(long timeStart) {
        Date date = new Date(timeStart);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        return format.format(date);
    }

    public String getDate(long timeStart) {
        Date date = new Date(timeStart);
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        return format.format(date);
    }
}
