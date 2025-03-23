package org.example.core.models.shedule;

import org.example.core.models.ComputeResource;

import java.util.Objects;

public class ScheduleInterval {
    ScheduleTimeStamp start;
    ScheduleTimeStamp end;
    ComputeResource computeResource;


    public boolean contains(ScheduleTimeStamp timestamp) {
        return start.compareTo(timestamp) <= 0 && end.compareTo(timestamp) > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass()) return false;
        var interval = (ScheduleInterval) o;
        return Objects.equals(start, interval.start) &&
                Objects.equals(end, interval.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    public ScheduleTimeStamp getStart() {
        return start;
    }

    public void setStart(ScheduleTimeStamp start) {
        this.start = start;
    }

    public ScheduleTimeStamp getEnd() {
        return end;
    }

    public void setEnd(ScheduleTimeStamp end) {
        this.end = end;
    }

    public ComputeResource getComputeResource() {
        return computeResource;
    }

    public void setComputeResource(ComputeResource computeResource) {
        this.computeResource = computeResource;
    }
}
