package org.example.core.models.commands.host_executor;

import org.example.core.models.commands.CommandResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public abstract class CommandExecutorBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandExecutorBase.class);

    protected abstract ProcessBuilder getProcessBuilder(String command);

    public CommandResult executeCommand(String command) throws IOException, InterruptedException {
        var processBuilder = getProcessBuilder(command);

        Process process = processBuilder.start();

        var output = new ArrayList<String>();
        var errorOutput = new ArrayList<String>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            LOGGER.debug("Command Output:");
            while ((line = reader.readLine()) != null) {
                output.add(line);
                LOGGER.debug(line);
            }
        }

        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            LOGGER.debug("Error Output:");
            while ((line = errorReader.readLine()) != null) {
                LOGGER.debug(line);
                errorOutput.add(line);
            }
        }

        int exitCode = process.waitFor();
        LOGGER.debug("Exit Code: " + exitCode);

        return new CommandResult(output, errorOutput, exitCode);
    }
}
