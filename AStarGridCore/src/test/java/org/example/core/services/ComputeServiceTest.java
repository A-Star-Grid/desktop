package org.example.core.services;

import org.example.core.clients.ServerClient;
import org.example.core.configurations.AppSettings;
import org.example.core.models.ComputingTask;
import org.example.core.services.settings.ApplicationSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComputeServiceTest {
    @Mock SubscribeService          subscribeService;
    @Mock
    ServerClient serverClient;
    @Mock PreferencesStorage        preferencesStorage;
    @Mock VirtualMachineFactory     virtualMachineFactory;
    @Mock ApplicationSettingsService applicationSettingsService;

    AppSettings   appSettings;
    ComputeService service;

    @BeforeEach
    void setUp() throws Exception {
        appSettings = new AppSettings();
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
    @DisplayName("process(): вычисления выключены → все таски отменены, ВМ остановлена")
    void process_inactive_cancelsTasksAndStopsVm() throws Exception {
        when(applicationSettingsService.isComputationActive()).thenReturn(false);

        Future<?> future = mock(Future.class);
        when(future.isDone()).thenReturn(false);

        ComputingTask task = mock(ComputingTask.class);
        when(task.getProjectId()).thenReturn(123);

        Field f = ComputeService.class.getDeclaredField("runningTasks");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        var running = (ConcurrentHashMap<ComputingTask, Future<?>>) f.get(service);

        running.put(task, future);

        service.process();

        verify(future).cancel(true);
        verify(virtualMachineFactory).stopVirtualMachine();
        assertTrue(running.isEmpty());
    }

    @Test
    @DisplayName("process(): вычисления включены, подписок нет → ВМ создаётся, задач нет")
    void process_activeWithNoSubs_createsVmOnly() {
        when(applicationSettingsService.isComputationActive()).thenReturn(true);

        when(subscribeService.getSubscribes()).thenReturn(List.of());
        when(virtualMachineFactory.createVirtualMachineIfNotExist())
                .thenReturn(mock(VirtualMachineFactory.VirtualMachine.class));

        service.process();

        verify(virtualMachineFactory).createVirtualMachineIfNotExist();
        verify(virtualMachineFactory, never()).startVirtualMachineIfNotRunning();
    }
}
