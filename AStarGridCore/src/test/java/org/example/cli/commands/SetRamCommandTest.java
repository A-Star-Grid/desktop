package org.example.cli.commands;

import org.example.cli.ServerClient;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class SetRamCommandTest {

    @Test
    void testValidRam() {
        ServerClient client = mock(ServerClient.class);
        when(client.setRamLimit(2048)).thenReturn("Set");

        var cmd = new SetRamCommand(client);
        cmd.setArgs(new String[]{"cmd", "--ram", "2048"});
        cmd.execute();

        verify(client).setRamLimit(2048);
    }

    @Test
    void testInvalidRam() {
        var cmd = new SetRamCommand(mock(ServerClient.class));
        cmd.setArgs(new String[]{"cmd", "--ram", "0"});
        cmd.execute(); // Expect error message
    }
}
