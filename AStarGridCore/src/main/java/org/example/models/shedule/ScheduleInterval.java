package org.example.models.shedule;

import org.example.models.ComputeResource;

public class ScheduleInterval {
    ScheduleTimeStamp start;
    ScheduleTimeStamp end;
    ComputeResource computeResource;


    public boolean contains(ScheduleTimeStamp timestamp) {
        return start.compareTo(timestamp) <= 0 && end.compareTo(timestamp) > 0;
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
