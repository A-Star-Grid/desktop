package org.example.core.models.shedule;

import org.example.core.models.ComputeResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleIntervalTest {

    private ScheduleTimeStamp ts(Day day, int seconds) {
        var t = new ScheduleTimeStamp();
        t.setDay(day);
        t.setTime(seconds);
        return t;
    }

    private ScheduleInterval interval(ScheduleTimeStamp start, ScheduleTimeStamp end) {
        var i = new ScheduleInterval();
        i.setStart(start);
        i.setEnd(end);
        i.setComputeResource(new ComputeResource()); // Можно заглушку / mock
        return i;
    }

    @Test
    @DisplayName("contains – момент внутри интервала")
    void contains_inside() {
        var i = interval(ts(Day.Monday, 10 * 3600),
                ts(Day.Monday, 12 * 3600));

        assertTrue(i.contains(ts(Day.Monday, 11 * 3600))); // 11:00
    }

    @Test
    @DisplayName("contains – границы: включён старт, исключён конец")
    void contains_bounds() {
        var i = interval(ts(Day.Monday, 10 * 3600),
                ts(Day.Monday, 12 * 3600));

        assertTrue(i.contains(ts(Day.Monday, 10 * 3600)));     // начало
        assertFalse(i.contains(ts(Day.Monday, 12 * 3600)));    // конец
    }

    @Test
    @DisplayName("contains – момент вне интервала")
    void contains_outside() {
        var i = interval(ts(Day.Monday, 10 * 3600),
                ts(Day.Monday, 12 * 3600));

        assertFalse(i.contains(ts(Day.Monday, 9 * 3600 + 59)));
    }

    @Test
    @DisplayName("equals и hashCode учитывают только начало и конец")
    void equalsAndHashCode() {
        var a = interval(ts(Day.Tuesday, 1), ts(Day.Tuesday, 2));
        var b = interval(ts(Day.Tuesday, 1), ts(Day.Tuesday, 2));
        var c = interval(ts(Day.Wednesday, 1), ts(Day.Wednesday, 2));

        // computeResource может отличаться
        b.setComputeResource(null);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }
}

