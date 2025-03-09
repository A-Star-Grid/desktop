package org.example;

import org.example.CommandFactory;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Использование: java -jar my-tool.jar <команда> [параметры]");
            System.out.println("Доступные команды: login, list-projects, set-ram, set-cpu, set-disk, subscribe, unsubscribe, get-subscribes");
            return;
        }

        CommandFactory commandFactory = new CommandFactory();
        Runnable command = commandFactory.getCommand(args);

        if (command != null) {
            command.run();
        } else {
            System.out.println("Ошибка: неизвестная команда.");
        }
    }
}
