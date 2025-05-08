package org.example.core.services;

import org.example.core.configurations.AppSettings;
import org.example.core.configurations.VBoxConfig;
import org.example.core.models.VirtualMachineState;
import org.example.core.models.commands.host_executor.ICommandExecutor;
import org.example.core.services.settings.ApplicationSettingsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShutdownHookTest {

    @Mock VBoxConfig vBoxConfig;
    @Mock ICommandExecutor commandExecutor;
    @Mock
    ApplicationSettingsService applicationSettingsService;
    @Mock PreferencesStorage preferencesStorage;
    @Mock AppSettings appSettings;

    private VirtualMachineFactory createOuterFactory() {
        when(preferencesStorage.getDeviceUUID()).thenReturn(UUID.randomUUID());
        return new VirtualMachineFactory(
                vBoxConfig, commandExecutor, applicationSettingsService, preferencesStorage, appSettings);
    }

    @Nested
    @DisplayName("onShutdown behaviour")
    class OnShutdown {

        @Test
        @DisplayName("Если виртуальная машина отсутствует — stopVirtualMachine НЕ вызывается")
        void noVm_nothingHappens() {
            VirtualMachineFactory factoryMock = mock(VirtualMachineFactory.class);
            when(factoryMock.getVirtualMachine()).thenReturn(Optional.empty());

            ShutdownHook hook = new ShutdownHook(factoryMock);

            hook.onShutdown();

            verify(factoryMock, never()).stopVirtualMachine();
        }

        @Test
        @DisplayName("Если виртуальная машина есть — вызывается stopVirtualMachine")
        void vmExists_stopCalled() {
            VirtualMachineFactory outer = createOuterFactory();

            VirtualMachineFactory.VirtualMachine vm =
                    outer.new VirtualMachine("localhost", "vm-test", 2, 2048, 30,
                            List.of(), "testDiskPath", VirtualMachineState.RUNNING);

            VirtualMachineFactory factoryMock = mock(VirtualMachineFactory.class);
            when(factoryMock.getVirtualMachine()).thenReturn(Optional.of(vm));

            doNothing().when(factoryMock).stopVirtualMachine();

            ShutdownHook hook = new ShutdownHook(factoryMock);

            hook.onShutdown();

            verify(factoryMock).stopVirtualMachine();
        }
    }
}
