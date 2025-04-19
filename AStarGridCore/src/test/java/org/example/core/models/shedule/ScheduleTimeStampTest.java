package org.example.core.models.shedule;

import org.example.core.models.shedule.Day;
import org.example.core.models.shedule.ScheduleTimeStamp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleTimeStampTest {

    private ScheduleTimeStamp ts(Day day, int seconds) {
        var t = new ScheduleTimeStamp();
        t.setDay(day);
        t.setTime(seconds);
        return t;
    }

    @Test
    @DisplayName("compareTo корректно сортирует моменты на одном дне")
    void compareTo_sameDay() {
        var t1 = ts(Day.Monday, 10 * 3600);   // 10:00
        var t2 = ts(Day.Monday, 12 * 3600);   // 12:00

        assertTrue(t1.compareTo(t2) < 0);
        assertTrue(t2.compareTo(t1) > 0);
        assertEquals(0, t1.compareTo(t1));
    }

    @Test
    @DisplayName("compareTo‑сначала сравнивает дни, затем время")
    void compareTo_differentDays() {
        var mondayMorning = ts(Day.Monday, 0);
        var tuesdayNight  = ts(Day.Tuesday, 23 * 3600 + 59);

        assertTrue(mondayMorning.compareTo(tuesdayNight) < 0);
        assertTrue(tuesdayNight.compareTo(mondayMorning) > 0);
    }

    @Test
    @DisplayName("equals и hashCode учитывают день и время")
    void equalsAndHashCode() {
        var a = ts(Day.Friday, 42);
        var b = ts(Day.Friday, 42);
        var c = ts(Day.Saturday, 42);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }
}

