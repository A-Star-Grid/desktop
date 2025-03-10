package org.example.models;

public class ScheduleInterval {
    private TimeSlot start;
    private TimeSlot end;
    private ComputeResource computeResource;

    public ScheduleInterval(TimeSlot start, TimeSlot end, ComputeResource computeResource) {
        this.start = start;
        this.end = end;
        this.computeResource = computeResource;
    }

    public TimeSlot getStart() {
        return start;
    }

    public TimeSlot getEnd() {
        return end;
    }

    public ComputeResource getComputeResource() {
        return computeResource;
    }
}

