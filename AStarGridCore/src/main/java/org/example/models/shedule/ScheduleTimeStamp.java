package org.example.models.shedule;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class ScheduleTimeStamp implements Comparable<ScheduleTimeStamp>{
    private Day day;
    private Integer time;

    public ScheduleTimeStamp() {
        day = Day.Monday;
    }

    @Override
    public int compareTo(ScheduleTimeStamp other) {
        var dayComparison = Integer.compare(this.day.ordinal(), other.day.ordinal());
        if (dayComparison != 0) {
            return dayComparison;
        }
        return Integer.compare(this.time, other.time);
    }



    public static ScheduleTimeStamp now() {
        var now = LocalDateTime.now(ZoneId.systemDefault());
        var dayOfWeek = now.getDayOfWeek();
        var secondsSinceMidnight = now.toLocalTime().toSecondOfDay();

        var timestamp = new ScheduleTimeStamp();
        timestamp.setDay(mapDayOfWeek(dayOfWeek));
        timestamp.setTime(secondsSinceMidnight);

        return timestamp;
    }

    private static Day mapDayOfWeek(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> Day.Monday;
            case TUESDAY -> Day.Tuesday;
            case WEDNESDAY -> Day.Wednesday;
            case THURSDAY -> Day.Thursday;
            case FRIDAY -> Day.Friday;
            case SATURDAY -> Day.Saturday;
            case SUNDAY -> Day.Sunday;
            default -> throw new IllegalStateException("Unexpected value: " + dayOfWeek);
        };
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
