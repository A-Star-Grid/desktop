package org.example.core.models.commands;

import java.util.ArrayList;
import java.util.List;

public class CommandResult {
    private final List<String> stdout;
    private final List<String> stderr;
    private final int exitCode;

    public CommandResult(String stdout, String stderr, int exitCode) {
        this(List.of(stdout),List.of(stderr),exitCode);
    }

    public CommandResult(List<String> stdout, List<String> stderr, int exitCode) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.exitCode = exitCode;
    }

    public String getStdout() {
        return String.join("\n", stdout);
    }

    public String getStderr() {
        return String.join("\n", stderr);
    }

    public List<String> getListStdout() {
        return stdout;
    }

    public List<String> getListStderr() {
        return stderr;
    }

    public int getExitCode() {
        return exitCode;
    }

    public boolean isSuccess() {
        return exitCode == 0;
    }

    @Override
    public String toString() {
        return "Exit Code: " + exitCode +
                "\nSTDOUT:\n" + stdout +
                "\nSTDERR:\n" + stderr;
    }
}

