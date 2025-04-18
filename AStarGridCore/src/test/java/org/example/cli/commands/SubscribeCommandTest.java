package org.example.cli.commands;

import org.example.cli.ServerClient;
import org.example.cli.models.ComputeResource;
import org.example.cli.models.ScheduleInterval;
import org.example.cli.models.SubscribeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubscribeCommandTest {

    @Spy
    @InjectMocks
    SubscribeCommand command;

    @Mock
    ServerClient mockClient;

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Подмена System.out, чтобы перехватывать вывод
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // Подмена клиента на мок
        // через reflection, т.к. он final
        try {
            var field = SubscribeCommand.class.getDeclaredField("client");
            field.setAccessible(true);
            field.set(command, mockClient);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Ошибка, если не передан --projectId")
    void execute_shouldPrintErrorIfProjectIdMissing() {
        command.setArgs(new String[] {
                "subscribe", "--cpu", "2", "--disk", "2048"
        });

        command.execute();

        assertTrue(outContent.toString().contains("Error: need --projectId"));
        verify(mockClient, never()).subscribeToProject(any());
    }

    @Test
    @DisplayName("Успешный парсинг аргументов и вызов клиента")
    void setArgsAndExecute_shouldBuildRequestAndCallClient() {
        String[] args = {
                "subscribe",
                "--projectId", "123",
                "--start-day", "Tuesday",
                "--end-day", "Tuesday",
                "--start-time", "10:00:00",
                "--end-time", "11:00:00",
                "--cpu", "4",
                "--disk", "8192",
                "--ram", "2048"
        };

        command.setArgs(args);

        when(mockClient.subscribeToProject(any())).thenReturn("Subscribed OK");

        command.execute();

        // Проверяем, что клиент был вызван с корректным объектом
        ArgumentCaptor<SubscribeRequest> captor = ArgumentCaptor.forClass(SubscribeRequest.class);
        verify(mockClient).subscribeToProject(captor.capture());

        SubscribeRequest req = captor.getValue();
        assertEquals(123, req.getProjectId());
        assertEquals(1, req.getScheduleIntervals().size());

        ScheduleInterval interval = req.getScheduleIntervals().get(0);
        assertEquals("Tuesday", interval.getStart().getDay());
        assertEquals(36000, interval.getStart().getTime()); // 10:00:00
        assertEquals(39600, interval.getEnd().getTime());   // 11:00:00

        ComputeResource res = interval.getComputeResource();
        assertEquals(4, res.getCpuCores());
        assertEquals(8192, res.getDiskSpace());
        assertEquals(2048, res.getRam());

        assertTrue(outContent.toString().contains("Subscribed OK"));
    }
}
