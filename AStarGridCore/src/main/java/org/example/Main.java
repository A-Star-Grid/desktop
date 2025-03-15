package org.example;

import java.io.IOException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0) {
            String mode = args[0];
            if ("cli".equalsIgnoreCase(mode)) {
                org.example.cli.Main.main(Arrays.copyOfRange(args, 1, args.length));
            }
        } else {
            var guiThread = new Thread(() -> {
                try {
                    org.example.gui.GuiServer.main(new String[]{});
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            guiThread.start();

            org.example.server.Main.main(new String[]{});
        }
    }
}
