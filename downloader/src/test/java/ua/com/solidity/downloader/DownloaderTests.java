package ua.com.solidity.downloader;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ua.com.solidity.common.FilteredTextInputStream;
import ua.com.solidity.common.Utils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class DownloaderTests {
    /*
    @Autowired
    private DataGovUaSourceInfo mainSourceInfo;

    public static String[] apiKeys = {
            "vidomosti-pro-spravi-pro-bankrutstvo-1",
            "1e2d305c-6ecb-48d7-908c-b3c27b8a9acf",
            null,
            "0e347d45-4db5-44ee-a14a-533d7cf17d7b",
            "8e7a4472-a010-4656-bfe9-60959ba5209d",
            "6c0eb6c0-d19a-4bb0-869b-3280df46800a",
            "506734bf-2480-448c-a2b4-90b6d06df11e",
            "7c51c4a0-104b-4540-a166-e9fc58485c1b",
            "470196d3-4e7a-46b0-8c0c-883b74ac65f0",
            "44e1d462-5de4-40e5-b722-46f2aa9a1e81",
            "672e0841-e1a2-47ec-b8d4-22839c71f4b3",
            "ab09ed00-4f51-4f6c-a2f7-1b2fb118be0f",
            "b465b821-db5d-4b8b-8131-12682fab2203",
            "c29e704a-b745-4669-97cd-3a345f437ad1",
            "8faa71c1-3a54-45e8-8f6e-06c92b1ff8bc",
            null,
            "1c7f3815-3259-45e0-bdf1-64dca07ddc10",
            "46ec7001-f492-4af8-8cae-00ba5e5537ce",
            "06779371-308f-42d7-895e-5a39833375f0",
            "4c65d66d-1923-4682-980a-9c11ce7ffdfe"
    };*/
/*
    @Test
    void TestDataGovUaApiKeys() {
        StringBuilder b = new StringBuilder();
        int count = 0;
        for (int i = 0; i < apiKeys.length; ++i) {
            b.append(MessageFormat.format("\n{0}: ", i + 1));
            if (apiKeys[i] == null) {
                b.append("IGNORED");
                ++count;
                continue;
            }

            mainSourceInfo.initialize(apiKeys[i]);
            if (mainSourceInfo.isValid()) {
                b.append(MessageFormat.format("apiKey: {0}, format: {1}, UTC : {2}, size: {3}, url: {4}",
                        mainSourceInfo.getApiKey(), (mainSourceInfo.isZipped() ? "zip/" : "") + mainSourceInfo.getFormat(),
                        mainSourceInfo.getRevisionDateTime().toInstant(), mainSourceInfo.getSize(), mainSourceInfo.getUrl()));
                ++count;
            } else {
                b.append(MessageFormat.format("apiKey: {} - is not valid.", mainSourceInfo.getApiKey()));
            }
        }
        log.info("resource data: {}", b);
        assertThat(count == apiKeys.length).as("DataSources failed: {} from {}", apiKeys.length - count, apiKeys.length).isTrue();
    }*/
/*
    @Test
    void TestFileDownloading() {
        File file = new File("D:/GovUa/mvswantedperson-photo-555.json");
        try (FileInputStream stream = new FileInputStream(file)) {
            ObjectMapper mapper = new ObjectMapper();
            log.info("Starting load jsonNode 40M...");
            JsonNode node = Utils.getJsonNode(stream, "UTF-8");
            while (node.elements().hasNext()) {
                System.out.println("");
            }
            assertThat(node).isNotNull();
            log.info("Loading finished.");
        } catch (Exception e) {
            assertThat(false).isTrue();
        }
    }

    @Test
    void xmlParseTest() {
        try(FilteredTextInputStream stream = new FilteredTextInputStream(new FileInputStream("D:/GovUa/test.xml"), 8192)) {
            XMLInputFactory factory = XMLInputFactory.newFactory();
            XMLStreamReader reader = factory.createXMLStreamReader(stream);
            XmlMapper mapper = new XmlMapper();
            JsonNode node = null;

            try {
                while (reader.hasNext()) {
                    switch (reader.next()) {
                        case XMLStreamReader.START_ELEMENT:
                            if (reader.getName().toString() == "SUBJECT") {
                                try {
                                    node = mapper.readValue(reader, JsonNode.class);
                                } catch (JsonParseException e) {
                                    JsonLocation location = e.getLocation();
                                    log.warn("\nposition: (row: {}, col: {})\n{}",
                                            location.getLineNr(), location.getColumnNr(),
                                            stream.getInfoNearLocation(location.getLineNr() - 1,location.getColumnNr() - 1,
                                            2, true, reader.getEncoding()), e);
                                    log.warn("parsing error near {} and \n node: {}", stream.getPosition(), node, e);
                                }
                            }
                            break;
                        case XMLStreamReader.END_ELEMENT:
                            break;
                    }
                }
            } catch (Exception e) {
                log.warn("Error found", e);
            }
        } catch (Exception e) {
            log.error("Something wrong.", e);
        }
    }

    @Test
    void ZippedXMLTest() {
        try (ZipFile zipFile = new ZipFile("D:/GovUa/17-ufop-full-20.zip")) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            ZipEntry firstFileEntry = null;

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (firstFileEntry == null && !entry.isDirectory()) firstFileEntry = entry;
                log.info("{} : {}", entry.isDirectory() ? "directory" : "file", entry.getName());
            }

            if (firstFileEntry != null) {
                try(InputStream inputStream = zipFile.getInputStream(firstFileEntry)) {
                    try (FileOutputStream outputStream = new FileOutputStream("D:/GovUa/test.xml", false)) {
                        log.info("start unzipping test");
                        Utils.streamCopy(inputStream, outputStream);
                        log.info("end unzipping test");
                    } catch (Exception e) {
                        log.error("Something wrong.", e);
                    }
                } catch (Exception e) {
                    log.error("Something wrong", e);
                }

                try(FileInputStream inputStream = new FileInputStream("D:/GovUa/test.xml")) {
                    XmlMapper xmlMapper = new XmlMapper();
                    log.info("Run XML downloading...");
                    XmlFactory factory = new XmlFactory();
                    JsonNode node = xmlMapper.readTree(inputStream);
                    log.info("XML downloaded.");
                } catch (Exception e) {
                    log.error("Something wrong.", e);
                }
            }
        } catch (Exception e) {
            assertThat(false).isTrue();
        }
    }*/

    @Test
    void checkDownloader() {

    }
}
