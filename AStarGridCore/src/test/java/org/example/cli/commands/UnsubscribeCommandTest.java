package org.example.cli.commands;

import org.example.cli.ServerClient;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class UnsubscribeCommandTest {

    @Test
    void testValidUnsubscribe() {
        ServerClient client = mock(ServerClient.class);
        var cmd = new UnsubscribeCommand(client);

        cmd.setArgs(new String[]{"cmd", "--projectId", "123"});
        cmd.execute();

        verify(client).unsubscribeFromProject(123);
    }

    @Test
    void testInvalidUnsubscribe() {
        var cmd = new UnsubscribeCommand(mock(ServerClient.class));
        cmd.setArgs(new String[]{"cmd", "--projectId", "0"});
        cmd.execute(); // Expect error message
    }
}
