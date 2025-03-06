package org.example.clients;

import com.jcraft.jsch.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class SshClient {
    private final String host;
    private final String username;
    private final String password;

    public SshClient(String host, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
    }

    public String executeCommand(String command) throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, 22);
        session.setPassword(password);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect(10_000);

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.setInputStream(null);

        // Создаем потоки для чтения стандартного и ошибочного вывода
        InputStream stdout = channel.getInputStream();
        InputStream stderr = channel.getErrStream();

        channel.connect();

        // Читаем оба потока
        String output = readStream(stdout);
        String error = readStream(stderr);

        channel.disconnect();
        session.disconnect();

        // Если stderr не пуст, добавляем его в ответ
        if (!error.isEmpty()) {
            System.out.println("STDOUT:\n" + output + "\nSTDERR:\n" + error);
            return "STDOUT:\n" + output + "\nSTDERR:\n" + error;
        }
        return output;
    }

    private String readStream(InputStream stream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = stream.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }

        return baos.toString(StandardCharsets.UTF_8);
    }
}
