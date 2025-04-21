package org.example.cli.commands;

import org.example.cli.ServerClient;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class GetProjectsCommandTest {

    @Test
    void testDefaultArgs() {
        ServerClient client = mock(ServerClient.class);
        when(client.getProjects(1, 5)).thenReturn("Projects");

        var cmd = new GetProjectsCommand(client);
        cmd.setArgs(new String[]{"cmd"});
        cmd.execute();

        verify(client).getProjects(1, 5);
    }

    @Test
    void testCustomArgs() {
        ServerClient client = mock(ServerClient.class);
        when(client.getProjects(3, 10)).thenReturn("Custom");

        var cmd = new GetProjectsCommand(client);
        cmd.setArgs(new String[]{"cmd", "--page", "3", "--perPage", "10"});
        cmd.execute();

        verify(client).getProjects(3, 10);
    }
}
