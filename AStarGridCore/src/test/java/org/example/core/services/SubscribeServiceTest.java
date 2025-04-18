package org.example.core.services;

import org.example.core.clients.ServerClient;
import org.example.core.models.ComputeResource;
import org.example.core.models.dto.SubscribeRequest;
import org.example.core.models.dto.SubscribeResponse;
import org.example.core.models.dto.SubscribeTransport;
import org.example.core.models.shedule.Day;
import org.example.core.models.shedule.ScheduleInterval;
import org.example.core.models.shedule.ScheduleTimeStamp;
import org.example.core.services.settings.VmSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubscribeServiceTest {

    @Mock ServerClient serverClient;
    @Mock PreferencesStorage preferencesStorage;
    @Mock VmSettingsService vmSettingsService;

    SubscribeService service;

    @BeforeEach
    void setUp() {
        when(preferencesStorage.getDeviceUUID()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000007"));
        service = new SubscribeService(serverClient, preferencesStorage, vmSettingsService);
    }

    private ScheduleTimeStamp ts(Day d, int seconds) {
        var t = new ScheduleTimeStamp();
        t.setDay(d);
        t.setTime(seconds);
        return t;
    }

    private ScheduleInterval interval(int startSec, int endSec, ComputeResource cr) {
        var i = new ScheduleInterval();
        i.setStart(ts(Day.Monday, startSec));
        i.setEnd  (ts(Day.Monday, endSec));
        i.setComputeResource(cr);
        return i;
    }

    @Test
    @DisplayName("getSubscribes – возвращает список ровно тот, что дал ServerClient")
    void getSubscribes_delegates() {
        var dummy = List.of(mock(SubscribeResponse.class));
        when(serverClient.getSubscribes(any())).thenReturn(Mono.just(dummy));

        List<SubscribeResponse> out = service.getSubscribes();

        assertSame(dummy, out);
        verify(serverClient).getSubscribes(preferencesStorage.getDeviceUUID());
    }

    @Test
    @DisplayName("getSubscribesByProjectId – фильтрует по projectId")
    void getSubscribesByProjectId_filters() {
        SubscribeResponse a = mock(SubscribeResponse.class);
        SubscribeResponse b = mock(SubscribeResponse.class);
        when(a.getProjectId()).thenReturn(1);
        when(b.getProjectId()).thenReturn(2);
        when(serverClient.getSubscribes(any()))
                .thenReturn(Mono.just(List.of(a, b)));

        List<SubscribeResponse> result = service.getSubscribesByProjectId(1);

        assertEquals(List.of(a), result);
    }

    @Test
    @DisplayName("unsubscribeFromProject – успешный ответ 200 OK")
    void unsubscribe_ok() {
        when(serverClient.unsubscribeFromProject(eq(42), any()))
                .thenReturn(Mono.just("done"));

        var resp = service.unsubscribeFromProject(42);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("done", resp.getBody());
    }

    @Nested
    @DisplayName("subscribe(): проверка ресурсных лимитов")
    class SubscribeLimits {
        private SubscribeRequest req(List<ScheduleInterval> ivals) {
            SubscribeRequest r = mock(SubscribeRequest.class);
            when(r.getScheduleIntervals()).thenReturn(ivals);
            return r;
        }

        private SubscribeResponse resp(int projectId, List<ScheduleInterval> ivals) {
            SubscribeResponse r = mock(SubscribeResponse.class);
            when(r.getProjectId()).thenReturn(projectId);
            when(r.getScheduleIntervals()).thenReturn(ivals);
            return r;
        }

        @Test
        @DisplayName("Если пиковое потребление превышает лимит — бросается IllegalArgumentException")
        void limitExceeded_throws() {
            var existing = resp(1, List.of(
                    interval(  0, 2000, new ComputeResource(1, 0, 0))));

            when(serverClient.getSubscribes(any())).thenReturn(Mono.just(List.of(existing)));

            var request = req(List.of(
                    interval(1000, 2500, new ComputeResource(2, 0, 0))));

            when(vmSettingsService.getCpuLimit()).thenReturn(2);   // лимит 2 CPU

            assertThrows(IllegalArgumentException.class,
                    () -> service.subscribe(request));

            verify(serverClient, never()).subscribeToProject(any());
        }

        @Test
        @DisplayName("Если ресурсы в пределах лимитов — подписка проходит и отправляется на сервер")
        void withinLimits_success() {
            when(serverClient.getSubscribes(any())).thenReturn(Mono.just(List.of())); // нет существующих

            SubscribeRequest request = req(List.of(
                    interval(0, 1000, new ComputeResource(2, 1024, 1024))));

            when(vmSettingsService.getCpuLimit()).thenReturn(4);
            when(vmSettingsService.getRamLimit()).thenReturn(8192);
            when(vmSettingsService.getDiskLimit()).thenReturn(10000);

            when(serverClient.subscribeToProject(any()))
                    .thenReturn(Mono.just("subscribed"));

            String result = service.subscribe(request);

            assertEquals("subscribed", result);

            ArgumentCaptor<SubscribeTransport> captor = ArgumentCaptor.forClass(SubscribeTransport.class);
            verify(serverClient).subscribeToProject(captor.capture());
            assertEquals(preferencesStorage.getDeviceUUID(), captor.getValue().getDeviceUuid());
        }
    }

    @Test
    @DisplayName("subscribe(): если получить текущие подписки не удалось (Mono.empty) — IllegalStateException")
    void cannotFetchCurrentSubs_throws() {
        when(serverClient.getSubscribes(any())).thenReturn(Mono.empty());
        SubscribeRequest request = mock(SubscribeRequest.class);

        assertThrows(IllegalStateException.class,
                () -> service.subscribe(request));
    }
}
