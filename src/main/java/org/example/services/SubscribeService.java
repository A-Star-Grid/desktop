package org.example.services;

import org.example.clients.ServerClient;
import org.example.models.ComputeResource;
import org.example.models.dto.SubscribeRequest;
import org.example.models.dto.SubscribeResponse;
import org.example.models.dto.SubscribeTransport;
import org.example.models.shedule.ScheduleTimeStamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class SubscribeService {
    private final ServerClient serverClient;
    private final PreferencesStorage preferencesStorage;
    private final SettingService settingService;

    @Autowired
    public SubscribeService(ServerClient serverClient,
                            PreferencesStorage preferencesStorage,
                            SettingService settingService) {
        this.serverClient = serverClient;
        this.preferencesStorage = preferencesStorage;
        this.settingService = settingService;
    }

    public ResponseEntity<List<SubscribeResponse>> getSubscribes() {
        var projectsMono = serverClient.getSubscribes(preferencesStorage.getDeviceUUID());
        var projects = projectsMono.block();

        return ResponseEntity.ok(projects);
    }

    public ResponseEntity<String> unsubscribeFromProject(Integer id) {
        var projectsMono = serverClient.unsubscribeFromProject(id, preferencesStorage.getDeviceUUID());
        var projects = projectsMono.block();

        if (projects != null) {
            return ResponseEntity.ok(projects);
        } else {
            return ResponseEntity.status(500).body("Failed to fetch projects");
        }
    }

    public ResponseEntity<String> subscribe(SubscribeRequest subscribeRequest) {
        var currentSubscriptions = serverClient.getSubscribes(preferencesStorage.getDeviceUUID()).block();

        if (currentSubscriptions == null) {
            throw new IllegalStateException("Failed to fetch existing subscriptions");
        }

        checkResourceLimits(currentSubscriptions, subscribeRequest);

        return ResponseEntity.ok(
                serverClient.subscribeToProject(
                                new SubscribeTransport(subscribeRequest, preferencesStorage.getDeviceUUID()))
                        .block());
    }

    private void checkResourceLimits(List<SubscribeResponse> existingSubscriptions, SubscribeRequest newRequest) {
        var resourceTimeline = new TreeMap<ScheduleTimeStamp, ComputeResource>();

        for (var subscription : existingSubscriptions) {
            var intervals = subscription.getScheduleIntervals();
            for (var interval : intervals) {
                resourceTimeline.put(interval.getStart(), interval.getComputeResource());
                resourceTimeline.put(interval.getEnd(), interval.getComputeResource().negative());
            }
        }

        var maxComputeResource = getMaxResourceUsage(resourceTimeline);

        if (maxComputeResource.getCpuCores() > settingService.getCpuLimit() ||
                maxComputeResource.getRam() > settingService.getRamLimit() ||
                maxComputeResource.getDiskSpace() > settingService.getDiskLimit()
        ) {
            throw new IllegalArgumentException("Overflow of resources limits");
        }
    }

    private ComputeResource getMaxResourceUsage(TreeMap<ScheduleTimeStamp, ComputeResource> resourceTimeline) {
        var currentResource = new ComputeResource();
        var maxComputeResource = new ComputeResource();

        for (Map.Entry<ScheduleTimeStamp, ComputeResource> entry : resourceTimeline.entrySet()) {
            var resource = entry.getValue();
            currentResource.add(resource);
            maxComputeResource = ComputeResource.max(currentResource, maxComputeResource);
        }

        return maxComputeResource;
    }
}
