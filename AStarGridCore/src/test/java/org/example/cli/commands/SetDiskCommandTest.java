package org.example.cli.commands;

import org.example.cli.ServerClient;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class SetDiskCommandTest {

    @Test
    void testValidDisk() {
        ServerClient client = mock(ServerClient.class);
        when(client.setDiskLimit(50)).thenReturn("Set");

        var cmd = new SetDiskCommand(client);
        cmd.setArgs(new String[]{"cmd", "--disk", "50"});
        cmd.execute();

        verify(client).setDiskLimit(50);
    }

    @Test
    void testInvalidDisk() {
        var cmd = new SetDiskCommand(mock(ServerClient.class));
        cmd.setArgs(new String[]{"cmd", "--disk", "0"});
        cmd.execute(); // Expect error message
    }
}
