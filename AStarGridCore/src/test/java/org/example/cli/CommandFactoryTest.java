package org.example.cli;

import org.example.cli.commands.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandFactoryTest {

    @Test
    void testKnownCommandsReturnCorrectTypes() {
        CommandFactory factory = new CommandFactory();

        assertInstanceOf(LoginCommand.class, factory.getCommand(new String[]{"login"}));
        assertInstanceOf(GetProjectsCommand.class, factory.getCommand(new String[]{"list-projects"}));
        assertInstanceOf(SetRamCommand.class, factory.getCommand(new String[]{"set-ram"}));
        assertInstanceOf(SetCpuCommand.class, factory.getCommand(new String[]{"set-cpu"}));
        assertInstanceOf(SetDiskCommand.class, factory.getCommand(new String[]{"set-disk"}));
        assertInstanceOf(ActivateComputationCommand.class, factory.getCommand(new String[]{"activate-computation"}));
        assertInstanceOf(ResetSettingsCommand.class, factory.getCommand(new String[]{"reset-settings"}));
        assertInstanceOf(SubscribeCommand.class, factory.getCommand(new String[]{"subscribe"}));
        assertInstanceOf(UnsubscribeCommand.class, factory.getCommand(new String[]{"unsubscribe"}));
        assertInstanceOf(GetSubscribesCommand.class, factory.getCommand(new String[]{"get-subscribes"}));
        assertInstanceOf(GetCurrentSettingsCommand.class, factory.getCommand(new String[]{"get-settings"}));
    }

    @Test
    void testUnknownCommandReturnsNull() {
        CommandFactory factory = new CommandFactory();
        Runnable result = factory.getCommand(new String[]{"unknown-command"});
        assertNull(result);
    }

    @Test
    void testEmptyArgsReturnsNull() {
        CommandFactory factory = new CommandFactory();
        Runnable result = factory.getCommand(new String[]{});
        assertNull(result);
    }
}
