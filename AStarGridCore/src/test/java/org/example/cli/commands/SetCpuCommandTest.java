package org.example.cli.commands;

import org.example.cli.ServerClient;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class SetCpuCommandTest {

    @Test
    void testValidCpu() {
        ServerClient client = mock(ServerClient.class);
        when(client.setCpuLimit(2.5)).thenReturn("Set");

        var cmd = new SetCpuCommand(client);
        cmd.setArgs(new String[]{"cmd", "--cpu", "2.5"});
        cmd.execute();

        verify(client).setCpuLimit(2.5);
    }

    @Test
    void testInvalidCpu() {
        var cmd = new SetCpuCommand(mock(ServerClient.class));
        cmd.setArgs(new String[]{"cmd", "--cpu", "0"});
        cmd.execute(); // Expect error message
    }
}
