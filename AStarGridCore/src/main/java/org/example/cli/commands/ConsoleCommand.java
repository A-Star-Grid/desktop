package org.example.cli.commands;

public interface ConsoleCommand extends Runnable {
    void execute();

    void setArgs(String[] args);

    @Override
    default void run() {
        execute();
    }
}
