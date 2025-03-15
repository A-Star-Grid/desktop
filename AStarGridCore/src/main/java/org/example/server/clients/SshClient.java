package org.example.server.clients;

import com.jcraft.jsch.*;
import org.example.server.models.commands.CommandResult;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class SshClient {
    private final String host;
    private final String username;
    private final String password;
    private final Integer port;

    public SshClient(String host, Integer port, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.port = port;
    }

    public CommandResult executeCommand(String command) throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, port);
        session.setPassword(password);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");

        // Современные KEX (Key Exchange)
        config.put("kex", "curve25519-sha256@libssh.org,curve25519-sha256,ecdh-sha2-nistp256,ecdh-sha2-nistp384,ecdh-sha2-nistp521,diffie-hellman-group-exchange-sha256,diffie-hellman-group14-sha256");

        // Шифрование (Cipher)
        config.put("cipher.s2c", "aes256-gcm@openssh.com,aes128-gcm@openssh.com,chacha20-poly1305@openssh.com,aes256-ctr,aes192-ctr,aes128-ctr");
        config.put("cipher.c2s", "aes256-gcm@openssh.com,aes128-gcm@openssh.com,chacha20-poly1305@openssh.com,aes256-ctr,aes192-ctr,aes128-ctr");

        // Message Authentication Code (MAC)
        config.put("mac.s2c", "hmac-sha2-512,hmac-sha2-256");
        config.put("mac.c2s", "hmac-sha2-512,hmac-sha2-256");

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
