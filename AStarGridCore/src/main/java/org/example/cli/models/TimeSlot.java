package org.example.cli.models;

public class TimeSlot {
    private String day;
    private int time;

    public TimeSlot(String day, int time) {
        this.day = day;
        this.time = time;
    }

    public String getDay() {
        return day;
    }

    public int getTime() {
        return time;
    }
}

