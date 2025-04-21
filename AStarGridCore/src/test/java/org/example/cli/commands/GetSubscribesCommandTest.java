package org.example.cli.commands;

import org.example.cli.ServerClient;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class GetSubscribesCommandTest {

    @Test
    void testExecute() {
        ServerClient client = mock(ServerClient.class);
        when(client.getSubscribes()).thenReturn("Subscribes");

        var cmd = new GetSubscribesCommand(client);
        cmd.setArgs(new String[]{});
        cmd.execute();

        verify(client).getSubscribes();
    }
}
