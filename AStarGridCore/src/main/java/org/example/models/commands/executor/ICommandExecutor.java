package org.example.models.commands.executor;

import java.io.IOException;
import java.util.ArrayList;

public interface ICommandExecutor {
    ArrayList<ArrayList<String>> executeCommand(String command) throws IOException, InterruptedException;
}
