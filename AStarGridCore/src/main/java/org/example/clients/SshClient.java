package org.example.clients;

import com.jcraft.jsch.*;
import org.example.models.commands.CommandResult;

import java.io.*;
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

    public CommandResult executeCommand(String command) throws Exception {
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

        InputStream stdout = channel.getInputStream();
        InputStream stderr = channel.getErrStream();

        channel.connect();

        String output = readStream(stdout);
        String error = readStream(stderr);

        // Ждем завершения команды и получаем код выхода
        while (!channel.isClosed()) {
            Thread.sleep(100);
        }
        int exitCode = channel.getExitStatus();

        channel.disconnect();
        session.disconnect();

        return new CommandResult(output, error, exitCode);
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
