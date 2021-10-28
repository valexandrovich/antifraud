package ua.com.solidity.otp.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.com.solidity.otp.repository.DataSourceRepository;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FtpClient {
    @Value("${ftp.host}")
    private String server;
    @Value("${ftp.port}")
    private int port;
    @Value("${ftp.user}")
    private String user;
    @Value("${ftp.password}")
    private String password;
    private FTPClient ftp;

    @PostConstruct
    private void init() {
        try {
            ftp = new FTPClient();
            ftp.connect(server, port);
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                throw new ExceptionInInitializerError("Exception in connecting to FTP Server");
            }
            ftp.login(user, password);
        } catch (IOException e) {
            log.error("Error connecting to FTP server! Server:{} Port:{} User:{}", server, port, user);
        }
    }

    public void close() throws IOException {
        ftp.disconnect();
    }

    public List<String> listFiles(String path) throws IOException {
        FTPFile[] files = ftp.listFiles(path);
        return Arrays.stream(files)
                .map(FTPFile::getName)
                .collect(Collectors.toList());

    }

    public void saveFile(File file, String path) throws IOException {
        createDirectories(path);
        ftp.storeFile(path, new FileInputStream(file));
    }

    private void createDirectories(String path) throws IOException {
        boolean dirExists = true;
        String[] directories = path.split("/");
        for (int i = 0; i < directories.length - 1; i++) {
            if (!directories[i].isEmpty()) {
                if (dirExists) {
                    dirExists = ftp.changeWorkingDirectory(directories[i]);
                }
                if (!dirExists) {
                    if (!ftp.makeDirectory(directories[i])) {
                        throw new IOException("Unable to create remote directory '" + directories[i] + "'.  error='" + ftp.getReplyString() + "'");
                    }
                    if (!ftp.changeWorkingDirectory(directories[i])) {
                        throw new IOException("Unable to change into newly created remote directory '" + directories[i] + "'.  error='" + ftp.getReplyString() + "'");
                    }
                }
            }
        }
        ftp.changeWorkingDirectory("/");
    }
}

