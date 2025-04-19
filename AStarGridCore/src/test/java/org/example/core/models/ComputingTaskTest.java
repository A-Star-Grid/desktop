package org.example.core.models;

import org.example.core.models.ComputingTask;
import org.example.core.models.shedule.ScheduleInterval;
import org.example.core.models.shedule.ScheduleTimeStamp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ComputingTaskTest {

    private ScheduleInterval dummyInterval(int start, int end) {
        ScheduleTimeStamp s = new ScheduleTimeStamp();
        s.setDay(org.example.core.models.shedule.Day.Monday);
        s.setTime(start);
        ScheduleTimeStamp e = new ScheduleTimeStamp();
        e.setDay(org.example.core.models.shedule.Day.Monday);
        e.setTime(end);

        ScheduleInterval interval = new ScheduleInterval();
        interval.setStart(s);
        interval.setEnd(e);
        return interval;
    }

    @Test
    @DisplayName("equals() и hashCode() работают правильно (taskUuid не влияет)")
    void testEqualsAndHashCode() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        ScheduleInterval interval = dummyInterval(0, 3600);

        ComputingTask a = new ComputingTask(1, interval, uuid1);
        ComputingTask b = new ComputingTask(1, interval, uuid2); // всё совпадает, кроме taskUuid

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());

        ComputingTask c = new ComputingTask(2, interval, uuid1);
        assertNotEquals(a, c);
    }

    @Test
    @DisplayName("геттеры возвращают корректные значения")
    void testGetters() {
        UUID uuid = UUID.randomUUID();
        ScheduleInterval interval = dummyInterval(100, 200);

        ComputingTask task = new ComputingTask(42, interval, uuid);

        assertEquals(42, task.getProjectId());
        assertEquals(interval, task.getInterval());
        assertEquals(uuid, task.getTaskUuid());
    }

    @Test
    @DisplayName("equals() на самом себе возвращает true")
    void testEqualsSelf() {
        ComputingTask task = new ComputingTask(1, dummyInterval(0, 10), UUID.randomUUID());
        assertEquals(task, task);
    }

    @Test
    @DisplayName("equals() на null и несовместимый тип возвращает false")
    void testEqualsNullAndWrongClass() {
        ComputingTask task = new ComputingTask(1, dummyInterval(0, 10), UUID.randomUUID());
        assertNotEquals(null, task);
        assertNotEquals("string", task);
    }
}
