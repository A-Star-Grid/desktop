package org.example.models.shedule;

public class ScheduleTimeStamp implements Comparable<ScheduleTimeStamp>{
    private Day day;
    private Integer time;

    @Override
    public int compareTo(ScheduleTimeStamp other) {
        int dayComparison = Integer.compare(this.day.ordinal(), other.day.ordinal());
        if (dayComparison != 0) {
            return dayComparison;
        }
        return Integer.compare(this.time, other.time);
    }

    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }
}
