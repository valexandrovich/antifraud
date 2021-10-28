package ua.com.solidity.otp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.expression.ParseException;
import org.springframework.stereotype.Service;
import ua.com.solidity.otp.model.ReserveCopyMessage;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
public class ReserveCopyService {
    final FtpClient ftpClient;

    public ReserveCopyService(FtpClient ftpClient) {
        this.ftpClient = ftpClient;
    }

    public void makeReserveCopy(ReserveCopyMessage message) {

        try {
            String filename = getFileName(message);
            File file = getFile(filename);
            String checksum = getOrCalculateFileChecksum(message, file);
            ftpClient.saveFile(file, buildFileTargetPath(file, checksum));
        } catch (IOException | NoSuchAlgorithmException e) {
            log.error(e.getMessage());
        }
    }

    private String getFileName(ReserveCopyMessage message) throws NotSerializableException {
        if (message.getFilename() == null) {
            throw new NotSerializableException("Error while parsing message!");
        }
        return message.getFilename();
    }

    private File getFile(String filename) throws FileNotFoundException {
        File file = new File(filename);
        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException("File " + filename + " not found!");
        }
        return file;
    }

    private String getOrCalculateFileChecksum(ReserveCopyMessage message, File file) throws IOException, NoSuchAlgorithmException {
        String checksum = message.getHash();
        if (checksum == null || checksum.length() != 32) {
            log.info("No checksum in file " + message.getFilename() + "! Calculating localy ...");
            checksum = getFileChecksum(file);
        }
        return checksum;
    }

    private String buildFileTargetPath(File file, String checksum) {
        StringBuilder targetPath = new StringBuilder();
        targetPath.append("/");
        for (int i = 0; i < 32; i = i + 8) {
            targetPath.append(checksum, i, i + 8);
            targetPath.append("/");
        }
        String[] path = file.getName().split("/");
        String filename = path[path.length - 1];
        targetPath.append(filename);
        return targetPath.toString();
    }

    private String getFileChecksum(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] byteArray = new byte[1024];
            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                md.update(byteArray, 0, bytesCount);
            }
        }
        byte[] bytes = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
