package org.example.cli.commands;

import org.example.cli.ServerClient;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class GetCurrentSettingsCommandTest {

    @Test
    void testExecute() {
        ServerClient client = mock(ServerClient.class);
        when(client.getCurrentSettings()).thenReturn("Settings");

        var cmd = new GetCurrentSettingsCommand(client);
        cmd.setArgs(new String[]{});
        cmd.execute();

        verify(client).getCurrentSettings();
    }
}
