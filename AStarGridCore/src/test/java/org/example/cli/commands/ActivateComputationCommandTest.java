package org.example.cli.commands;

import org.example.cli.ServerClient;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class ActivateComputationCommandTest {

    @Test
    void testEnable() {
        ServerClient client = mock(ServerClient.class);
        when(client.setComputationState(true)).thenReturn("Enabled");

        var cmd = new ActivateComputationCommand(client);
        cmd.setArgs(new String[]{"cmd", "--enable"});
        cmd.execute();

        verify(client).setComputationState(true);
    }

    @Test
    void testDisable() {
        ServerClient client = mock(ServerClient.class);
        when(client.setComputationState(false)).thenReturn("Disabled");

        var cmd = new ActivateComputationCommand(client);
        cmd.setArgs(new String[]{"cmd", "--disable"});
        cmd.execute();

        verify(client).setComputationState(false);
    }
}
