import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GuiServer {
    public static void main(String[] args) throws IOException {
        int port = 3000;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Раздача статических файлов из папки build/
        server.createContext("/", new StaticFileHandler("build"));

        server.setExecutor(null);
        server.start();
        System.out.println("🚀 React сервер запущен на http://localhost:" + port);
    }

    static class StaticFileHandler implements HttpHandler {
        private final String rootDir;

        public StaticFileHandler(String rootDir) {
            this.rootDir = rootDir;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html"; // Главная страница
            File file = new File(rootDir + path);

            // Если файл не найден, отдаем index.html (для поддержки React Router)
            if (!file.exists() || file.isDirectory()) {
                file = new File(rootDir + "/index.html");
            }

            // Определяем MIME-тип
            String contentType = Files.probeContentType(Paths.get(file.getPath()));
            if (contentType == null) contentType = "application/octet-stream";

            // Читаем файл и отправляем клиенту
            byte[] bytes = Files.readAllBytes(Paths.get(file.getPath()));
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();
        }
    }
}
