package org.example.core.models.commands.host_executor;

import org.example.core.models.commands.CommandResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public abstract class CommandExecutorBase {
    protected abstract ProcessBuilder getProcessBuilder(String command);

    public CommandResult executeCommand(String command) throws IOException, InterruptedException {
        var processBuilder = getProcessBuilder(command);

        Process process = processBuilder.start();

        var output = new ArrayList<String>();
        var errorOutput = new ArrayList<String>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            System.out.println("Command Output:");
            while ((line = reader.readLine()) != null) {
                output.add(line);
                System.out.println(line);
            }
        }

        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            System.out.println("Error Output:");
            while ((line = errorReader.readLine()) != null) {
                System.err.println(line);
                errorOutput.add(line);
            }
        }

        int exitCode = process.waitFor();
        System.out.println("Exit Code: " + exitCode);

        return new CommandResult(output, errorOutput, exitCode);
    }
}
