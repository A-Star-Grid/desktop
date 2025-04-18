package org.example.gui;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GuiServer {
    public static void main(String[] args) throws IOException {
        int port = 3000;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new StaticFileHandler(Path.of("app","build").toString()));

        server.setExecutor(null);
        server.start();
        System.out.println("React сервер запущен на http://localhost:" + port);

        openBrowser("http://localhost:" + port);
    }

    private static void openBrowser(String url) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            Runtime rt = Runtime.getRuntime();

            if (os.contains("win")) {
                rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                rt.exec("open " + url);
            } else if (os.contains("nix") || os.contains("nux")) {
                rt.exec("xdg-open " + url);
            } else {
                System.out.println("Неизвестная ОС, откройте браузер вручную: " + url);
            }
        } catch (Exception e) {
            System.out.println("Ошибка при попытке открыть браузер: " + e.getMessage());
        }
    }


    static class StaticFileHandler implements HttpHandler {
        private final String rootDir;

        public StaticFileHandler(String rootDir) {
            this.rootDir = rootDir;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            File file = new File(rootDir + path);

            if (!file.exists() || file.isDirectory()) {
                file = new File(rootDir + "/index.html");
            }

            String contentType = Files.probeContentType(Paths.get(file.getPath()));
            if (contentType == null) contentType = "application/octet-stream";

            byte[] bytes = Files.readAllBytes(Paths.get(file.getPath()));
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();
        }
    }
}
