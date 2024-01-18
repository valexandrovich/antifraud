package ua.com.valexa.downloaderismc.service.govua;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.ls.LSOutput;
import ua.com.valexa.common.dto.StepResponseDto;
import ua.com.valexa.dbismc.model.enums.StepStatus;
import ua.com.valexa.downloaderismc.service.Downloadable;
import ua.com.valexa.downloaderismc.service.DownloaderService;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service("govua")
@Slf4j
public class GovuaDownloader implements Downloadable {

    private final String PACKAGES_URL_PREFIX = "https://data.gov.ua/api/3/action/package_show?id=";
    private final String RESOURCES_URL_PREFIX = "https://data.gov.ua/api/3/action/resource_show?id=";

    final static int MAX_RETRIES = 10;

    @Value("${downloader.mountPoint}")
    private static String mountPoint;

    @Value("${downloader.proxyHost}")
    private String proxyHost;

    @Value("${downloader.proxyPort}")
    private Integer proxyPort;

    HttpHeaders headers = new HttpHeaders();
    RestTemplate restTemplate = new RestTemplate();
    ObjectMapper mapper = new ObjectMapper();

    @Override
    public StepResponseDto handleDownload(Long stepId, Map<String, String> parameters) {

        StepResponseDto stepResponseDto = new StepResponseDto();
        stepResponseDto.setStepId(stepId);

        try {
            log.debug("Parsing Step Request parameters");
            RequestMetadata requestMetadata = new RequestMetadata(stepId, parameters);
            log.debug("Getting package metadata " + requestMetadata.getSourceName());
            JsonNode packageMetadata = getPackageMetadata(requestMetadata.getPackageId());
            log.debug("Getting actual resource id " + requestMetadata.getSourceName());
            String actualResourceId = getActualResourceId(packageMetadata);
            log.debug("Getting actual revision metadata " + requestMetadata.getSourceName());
            GovUaRevisionMetadata metadata = getActualRevisionMetadata(actualResourceId);
            String fileName = mountPoint + System.getProperty("file.separator") + stepId + "_" + requestMetadata.getSourceName() + "." + metadata.getFileExtension();
            log.debug("Downloading file " + fileName + "  :  " + requestMetadata.getSourceName() + "; Link " + metadata.getFileUrl());
            downloadFile(metadata.fileUrl.toString(), fileName);
            stepResponseDto.getResults().put("file", fileName);
            stepResponseDto.setComment("Файл завантажено");
            stepResponseDto.setStatus(StepStatus.FINISHED);
        } catch (Exception e) {
            stepResponseDto.setStatus(StepStatus.FAILED);
            stepResponseDto.setComment(e.getMessage());
            log.error(e.getMessage());
        }
        return stepResponseDto;
    }


    public JsonNode getPackageMetadata(String packageId) throws Exception {
        headers.setContentType(MediaType.APPLICATION_JSON);
        String rawResponse = restTemplate.getForObject(PACKAGES_URL_PREFIX + packageId, String.class);
        JsonNode packageMetadata = null;
        packageMetadata = mapper.readTree(rawResponse);
        return packageMetadata;
    }

    public String getActualResourceId(JsonNode packageMetadata) throws Exception {
        JsonNode resources = packageMetadata.get("result").get("resources");
        String resourceId = resources.get(resources.size() - 1).get("id").textValue();
        return resourceId;
    }

    public GovUaRevisionMetadata getActualRevisionMetadata(String resourceId) throws Exception {
        GovUaRevisionMetadata metadata = new GovUaRevisionMetadata();
        String responseRaw = restTemplate.getForObject(RESOURCES_URL_PREFIX + resourceId, String.class);
        JsonNode node = null;
        node = mapper.readTree(responseRaw);
        String urlFile = node.get("result").get("url").textValue();
        metadata.setFileUrl(new URL(urlFile));
        metadata.setFileExtension(node.get("result").get("format").textValue().toLowerCase());
        metadata.setId(node.get("result").get("revision_id").textValue());
        return metadata;
    }


    public void downloadFile(String url, String filename) {
        Proxy proxy = null;
        long fileSize = 0;
        try {

            if (proxyHost != null && proxyPort != null) {
                log.debug("Configuring proxy: " + proxyHost + ":" + proxyPort);
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            } else {
                log.debug("No proxy needed");
            }
            HttpURLConnection conn;
            if (proxy != null) {
                conn = (HttpURLConnection) new URL(url).openConnection(proxy);
            } else {
                conn = (HttpURLConnection) new URL(url).openConnection();
            }

            conn.setRequestMethod("HEAD");
            fileSize = conn.getContentLengthLong();
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        try (FileOutputStream outputStream = new FileOutputStream(filename)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0;
            long startByte = 0;

            while (totalBytesRead < fileSize) {
                int retryCount = 0;
                boolean chunkDownloaded = false;

                while (!chunkDownloaded && retryCount < MAX_RETRIES) {
                    HttpURLConnection connection = null;
                    BufferedInputStream bis = null;
                    try {
                        if (proxy != null) {
                            connection = (HttpURLConnection) new URL(url).openConnection(proxy);
                        } else {
                            connection = (HttpURLConnection) new URL(url).openConnection();
                        }

                        connection = (HttpURLConnection) new URL(url).openConnection();
                        if (startByte > 0) {
                            connection.setRequestProperty("Range", "bytes=" + startByte + "-");
                        }

                        connection.connect();
                        bis = new BufferedInputStream(connection.getInputStream());

                        while ((bytesRead = bis.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                            totalBytesRead += bytesRead;
                            startByte += bytesRead;
                            printDownloadProgress(totalBytesRead, fileSize);
                        }

                        chunkDownloaded = true;
                    } catch (IOException e) {
                        retryCount++;
                        log.warn("Retry " + retryCount + " for chunk starting at byte " + startByte);
                    } finally {
                        if (bis != null) {
                            try {
                                bis.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                }

                if (!chunkDownloaded) {
                    throw new IOException("Failed to download chunk after " + MAX_RETRIES + " retries.");
                }
            }
            log.debug("File downloaded: " + filename);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printDownloadProgress(long totalBytesRead, long fileSize) {
        if (fileSize > 0) { // Avoid division by zero
            int progressPercentage = (int) ((totalBytesRead * 100) / fileSize);
            log.debug("Download progress: " + progressPercentage + "%" + "   " + totalBytesRead / 1024 + " kb / " + fileSize / 1024 + " kb");
        }
    }

    @Data
    private class GovUaRevisionMetadata {
        String id;
        String fileExtension;
        URL fileUrl;
    }

    @Data
    private class RequestMetadata {
        public RequestMetadata(Long stepId, Map<String, String> parameters) {
            if (parameters == null) {
                throw new IllegalArgumentException("Input map cannot be null");
            }

            try {

                this.sourceName = parameters.get("sourceName");
                if (this.sourceName == null || this.sourceName.isBlank()) {
                    throw new IllegalArgumentException("sourceName is missing or blank");
                }

                this.packageId = parameters.get("packageId");
                if (this.packageId == null || this.packageId.isBlank()) {
                    throw new IllegalArgumentException("packageId is missing or blank");
                }

                String retriesStr = parameters.get("retries");
                if (retriesStr == null || retriesStr.isBlank()) {
                    throw new IllegalArgumentException("retries is missing or blank");
                }
                this.retries = Long.parseLong(retriesStr);

            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Number format error: " + e.getMessage());
            }
        }

        Long stepId;
        String sourceName;
        String packageId;
        Long retries;

    }
}
