package com.sptech.school;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Reparo {
        private final String host;
        private final String user;
        private final String privateKey;

    public Reparo(String host, String user, String privateKey) {
        this.host = host;
        this.user = user;
        this.privateKey = privateKey;
    }

    public void reparar() throws IOException {
        Path key;
    try (SSHClient ssh = new SSHClient()) {
            ssh.addHostKeyVerifier(new PromiscuousVerifier());
            ssh.connect(host);

        key = Files.createTempFile("key", ".pem");
        Files.writeString(key, privateKey, StandardOpenOption.WRITE);

        KeyProvider keyProvider = ssh.loadKeys(key.toString());
        ssh.authPublickey(user, keyProvider);
        ssh.authPublickey(user, keyProvider);

            try (Session session = ssh.startSession()) {
                String[] commands = {
                        "sudo apt remove --purge -y python3 python3-pip",
                        "sudo apt autoremove -y",
                        "sudo rm -rf /usr/local/lib/python3.*/dist-packages/*",
                        "sudo apt update",
                        "sudo apt install -y python3 python3-pip",
                        "sudo pip3 install -r /home/" + user + "/Black-Scrip-Python/requirements.txt",
                        "sudo poweroff"
                };

                for (String cmd : commands) {
                    Session.Command command = session.exec(cmd);
                    command.join();
                }
            }

            ssh.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
