package com.example.notes.data.dto;

public class Note {
    private String content;
    private String date;
    private long timeInMillis;

    public Note(String content, String date, long timeInMillis) {
        this.content = content;
        this.date = date;
        this.timeInMillis = timeInMillis;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
