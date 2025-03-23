package org.example.core.models.commands.host_executor;

import org.example.core.models.commands.CommandResult;

import java.io.IOException;
import java.util.ArrayList;

public interface ICommandExecutor {
    CommandResult executeCommand(String command) throws IOException, InterruptedException;
}
