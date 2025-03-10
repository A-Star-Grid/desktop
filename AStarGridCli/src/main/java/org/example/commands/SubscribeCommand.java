package org.example.commands;

import org.example.ServerClient;
import org.example.models.ComputeResource;
import org.example.models.ScheduleInterval;
import org.example.models.SubscribeRequest;
import org.example.models.TimeSlot;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SubscribeCommand implements ConsoleCommand {
    private int projectId;
    private String startDay = "Monday";
    private String endDay = "Monday";
    private int startTime = 0;
    private int endTime = 3600;
    private int cpuCores = 1;
    private int diskSpace = 1024;
    private int ram = 512;
    private final ServerClient client = new ServerClient();

    @Override
    public void setArgs(String[] args) {
        for (int i = 1; i < args.length; i++) {
            try {
                switch (args[i]) {
                    case "--projectId" -> projectId = Integer.parseInt(args[i + 1]);
                    case "--start-day" -> startDay = args[i + 1];
                    case "--end-day" -> endDay = args[i + 1];
                    case "--start-time" -> startTime = parseTimeToSeconds(args[i + 1]);
                    case "--end-time" -> endTime = parseTimeToSeconds(args[i + 1]);
                    case "--cpu" -> cpuCores = Integer.parseInt(args[i + 1]);
                    case "--disk" -> diskSpace = Integer.parseInt(args[i + 1]);
                    case "--ram" -> ram = Integer.parseInt(args[i + 1]);
                }
            } catch (Exception e) {
                System.out.println("Error of argument: " + args[i]);
            }
        }
    }

    @Override
    public void execute() {
        if (projectId <= 0) {
            System.out.println("Error: need --projectId");
            return;
        }

        List<ScheduleInterval> scheduleIntervals = new ArrayList<>();
        scheduleIntervals.add(new ScheduleInterval(
                new TimeSlot(startDay, startTime),
                new TimeSlot(endDay, endTime),
                new ComputeResource(cpuCores, diskSpace, ram)
        ));

        SubscribeRequest request = new SubscribeRequest(projectId, scheduleIntervals);
        String response = client.subscribeToProject(request);
        System.out.println(response);
    }

    private int parseTimeToSeconds(String timeString) {
        try {
            LocalTime time = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm:ss"));
            return time.toSecondOfDay();
        } catch (Exception e) {
            throw new IllegalArgumentException("Messed time format. Use HH:mm:ss");
        }
    }
}
