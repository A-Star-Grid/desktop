package org.example.server.models.commands;

public class CommandResult {
    private final String stdout;
    private final String stderr;
    private final int exitCode;

    public CommandResult(String stdout, String stderr, int exitCode) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.exitCode = exitCode;
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() {
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

