package org.example.core.models;

import org.example.core.clients.ServerClient;
import org.example.core.configurations.AppSettings;
import org.example.core.models.ComputeResource;
import org.example.core.models.dto.SubscribeResponse;
import org.example.core.models.shedule.ScheduleInterval;
import org.example.core.services.ComputeService;
import org.example.core.services.PreferencesStorage;
import org.example.core.services.SubscribeService;
import org.example.core.services.VirtualMachineFactory;
import org.example.core.services.settings.ApplicationSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ComputeResourceTest {
    @Mock
    SubscribeService subscribeService;
    @Mock
    ServerClient serverClient;
    @Mock
    PreferencesStorage preferencesStorage;
    @Mock
    VirtualMachineFactory virtualMachineFactory;
    @Mock
    ApplicationSettingsService applicationSettingsService;

    ComputeService service;

    @BeforeEach
    void setup() {
        AppSettings appSettings = new AppSettings();
        appSettings.taskArchivesDirectory = "/tmp";

        service = new ComputeService(
                appSettings,
                subscribeService,
                serverClient,
                preferencesStorage,
                applicationSettingsService,
                virtualMachineFactory);
    }

    @Test
    @DisplayName("Конструктор без аргументов задаёт нули")
    void defaultConstructor_setsZeros() {
        ComputeResource cr = new ComputeResource();

        assertAll(
                () -> assertEquals(0, cr.getCpuCores()),
                () -> assertEquals(0, cr.getDiskSpace()),
                () -> assertEquals(0, cr.getRam())
        );
    }

    @Test
    @DisplayName("Конструктор с параметрами корректно проставляет поля")
    void constructorWithArgs_setsGivenValues() {
        ComputeResource cr = new ComputeResource(2, 50, 4096);

        assertAll(
                () -> assertEquals(2, cr.getCpuCores()),
                () -> assertEquals(50, cr.getDiskSpace()),
                () -> assertEquals(4096, cr.getRam())
        );
    }

    @Test
    @DisplayName("Конструктор копирования создаёт независимую копию")
    void copyConstructor_createsIndependentCopy() {
        ComputeResource original = new ComputeResource(1, 10, 1024);
        ComputeResource copy     = new ComputeResource(original);

        // меняем копию ‑ оригинал не меняется
        copy.setCpuCores(8);

        assertEquals(1, original.getCpuCores());
        assertEquals(8, copy.getCpuCores());
    }

    @Test
    @DisplayName("add увеличивает ресурсы текущего объекта и возвращает this")
    void add_addsValuesAndReturnsSelf() {
        ComputeResource a = new ComputeResource(1, 10, 1000);
        ComputeResource b = new ComputeResource(2, 5,  500);

        ComputeResource returned = a.add(b);

        assertSame(a, returned);
        assertAll(
                () -> assertEquals(3, a.getCpuCores()),
                () -> assertEquals(15, a.getDiskSpace()),
                () -> assertEquals(1500, a.getRam())
        );
    }

    @Test
    @DisplayName("subtract уменьшает ресурсы текущего объекта и возвращает this")
    void subtract_subtractsValues() {
        ComputeResource a = new ComputeResource(4, 40, 4000);
        ComputeResource b = new ComputeResource(1, 10,  500);

        a.subtract(b);

        assertAll(
                () -> assertEquals(3, a.getCpuCores()),
                () -> assertEquals(30, a.getDiskSpace()),
                () -> assertEquals(3500, a.getRam())
        );
    }

    @Test
    @DisplayName("negative меняет знак всех полей и возвращает this")
    void negative_invertsAllFields() {
        ComputeResource a = new ComputeResource(2, 30, 2048);

        a.negative();

        assertAll(
                () -> assertEquals(-2, a.getCpuCores()),
                () -> assertEquals(-30, a.getDiskSpace()),
                () -> assertEquals(-2048, a.getRam())
        );
    }

    @Test
    @DisplayName("max возвращает новый объект с максимумами по каждому полю, не изменяя исходные")
    void max_returnsPerFieldMaximums() {
        ComputeResource a = new ComputeResource(1, 50, 1024);
        ComputeResource b = new ComputeResource(2, 30, 2048);

        ComputeResource c = ComputeResource.max(a, b);

        assertAll(
                () -> assertEquals(2, c.getCpuCores()),
                () -> assertEquals(50, c.getDiskSpace()),
                () -> assertEquals(2048, c.getRam())
        );

        assertEquals(1, a.getCpuCores());
        assertEquals(30, b.getDiskSpace());
    }

    @Test
    void cancelAllRunningTasks_shouldCancelAllFutures() throws Exception {
        var task = mock(ComputingTask.class);
        var future = mock(Future.class);
        when(future.isDone()).thenReturn(false);

        var f = ComputeService.class.getDeclaredField("runningTasks");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        var running = (java.util.concurrent.ConcurrentHashMap<ComputingTask, Future<?>>) f.get(service);
        running.put(task, future);

        var cancelAllMethod = ComputeService.class.getDeclaredMethod("cancelAllRunningTasks");
        cancelAllMethod.setAccessible(true);
        cancelAllMethod.invoke(service);

        verify(future).cancel(true);
    }
}
