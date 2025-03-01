package org.example.models.commands.executor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public abstract class CommandExecutorBase {
    protected abstract ProcessBuilder getProcessBuilder(String command);

    public ArrayList<ArrayList<String>> executeCommand(String command) throws IOException, InterruptedException {
        var processBuilder = getProcessBuilder(command);

        Process process = processBuilder.start();

        var output = new ArrayList<String>();
        var errorOutput = new ArrayList<String>();
        var result = new ArrayList<ArrayList<String>>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            System.out.println("Command Output:");
            while ((line = reader.readLine()) != null) {
                output.add(line);
                System.out.println(line);
            }
        }
        result.add(output);

        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            System.out.println("Error Output:");
            while ((line = errorReader.readLine()) != null) {
                System.err.println(line);
                errorOutput.add(line);
            }
        }
        result.add(errorOutput);

        int exitCode = process.waitFor();
        System.out.println("Exit Code: " + exitCode);

        return result;
    }
}
