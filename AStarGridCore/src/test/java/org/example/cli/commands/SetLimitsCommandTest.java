package org.example.cli.commands;

import org.example.cli.ServerClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class SetLimitsCommandTest {

    ServerClient mockClient;
    SetLimitsCommand command;

    ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() throws Exception {
        mockClient = mock(ServerClient.class);

        command = new SetLimitsCommand();

        // Подставим мок клиента через reflection (final поле)
        var field = SetLimitsCommand.class.getDeclaredField("client");
        field.setAccessible(true);
        field.set(command, mockClient);

        // Перехват System.out
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @Test
    @DisplayName("Устанавливает все лимиты, если они переданы")
    void setAndExecute_allLimitsCalled() {
        String[] args = {
                "set-limits",
                "--ram", "2048",
                "--cpu", "2.5",
                "--disk", "10000"
        };

        command.setArgs(args);
        command.execute();

        verify(mockClient).setRamLimit(2048);
        verify(mockClient).setCpuLimit(2.5);
        verify(mockClient).setDiskLimit(10000);

        assertTrue(outContent.toString().contains("Success."));
    }

    @Test
    @DisplayName("Не вызывает setDiskLimit(), если не передан")
    void setAndExecute_partialLimits() {
        String[] args = {
                "set-limits",
                "--ram", "1024",
                "--cpu", "1.5"
        };

        command.setArgs(args);
        command.execute();

        verify(mockClient).setRamLimit(1024);
        verify(mockClient).setCpuLimit(1.5);
        verify(mockClient, never()).setDiskLimit(anyInt());

        assertTrue(outContent.toString().contains("Success."));
    }

    @Test
    @DisplayName("Игнорирует значения <= 0")
    void setAndExecute_zeroOrNegativeIgnored() {
        String[] args = {
                "set-limits",
                "--ram", "0",
                "--cpu", "-1",
                "--disk", "0"
        };

        command.setArgs(args);
        command.execute();

        verify(mockClient, never()).setRamLimit(anyInt());
        verify(mockClient, never()).setCpuLimit(anyDouble());
        verify(mockClient, never()).setDiskLimit(anyInt());

        assertTrue(outContent.toString().contains("Success."));
    }
}
