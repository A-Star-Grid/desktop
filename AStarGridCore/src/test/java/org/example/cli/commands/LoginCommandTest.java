
package org.example.cli.commands;

import org.example.cli.ServerClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class LoginCommandTest {

    private ServerClient mockClient;
    private LoginCommand loginCommand;

    @BeforeEach
    void setUp() {
        mockClient = mock(ServerClient.class);
        loginCommand = new LoginCommand(mockClient);
    }

    @Test
    void testSetArgs_parsesCorrectly() {
        String[] args = {"login", "--username", "testUser", "--password", "testPass"};
        loginCommand.setArgs(args);
        loginCommand.execute();
        verify(mockClient).login("testUser", "testPass");
    }

    @Test
    void testExecute_missingUsernameOrPassword_printsError() {
        String[] args = {"login", "--username"};
        loginCommand.setArgs(args);
        loginCommand.execute();
        verify(mockClient, never()).login(any(), any());
    }
}
