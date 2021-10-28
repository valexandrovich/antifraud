package ua.com.solidity.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Slf4j
public class Utils {

    private Utils() {
        // nothing
    }

    public static DateFormat outputDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSXXX");
    public static DateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static String removeIgnoredChars(String value, String ignoredChars) {
        int first = 0;
        while (first < value.length() && ignoredChars.indexOf(value.charAt(first)) >= 0) {
            ++first;
        }

        int last = value.length() - 1;
        while (last >= 0 && ignoredChars.indexOf(value.charAt(last)) >= 0) {
            --last;
        }
        return first < last ? value.substring(first, last + 1) : "";
    }

    public static boolean isQuotedString(String value, char quote) {
        return value.length() > 2 && value.charAt(0) == quote && value.charAt(value.length() - 1) == quote;
    }

    public static String normalizeString(String value, char quote, String ignoredChars) {
        value = removeIgnoredChars(value, ignoredChars);
        if (isQuotedString(value, quote)) {
            String quoteString = String.valueOf(quote);
            return value.substring(1, value.length() - 1).replace(quoteString + quoteString, quoteString);
        }
        return value;
    }

    public static InputStream getStreamFromUrl(String url) {
        URL u;
        try {
            u = new URL(url);
            HttpURLConnection httpConnection = (HttpURLConnection) u.openConnection();
            return httpConnection.getInputStream();
        } catch (Exception e) {
            log.error("Can't read url {}", url, e);
        }
        return null;
    }

    public static List<String> getPreview(InputStream stream, String encoding, int byteBufferLength) {
        List<String> result = new ArrayList<>();

        byte[] buffer = new byte[byteBufferLength];
        try {
            int len = stream.read(buffer);
            ByteArrayInputStream input = new ByteArrayInputStream(buffer, 0, len);

            try(InputStreamReader reader = new InputStreamReader(stream, encoding); Scanner scanner = new Scanner(reader)) {
                while(scanner.hasNextLine()) {
                    result.add(scanner.nextLine());
                }
            }
        } catch (Exception e) {
            log.warn("Reading preview error.", e);
        }

        return result;
    }

    public static String joinStrings(List<String> strings) {
        StringBuilder builder = new StringBuilder();
        for (String string : strings) {
            builder.append(string);
            builder.append("\n");
        }
        return builder.toString();
    }

    public static String joinStrings(String[] strings) {
        StringBuilder builder = new StringBuilder();
        for(String string : strings) {
            builder.append(string);
            builder.append("\n");
        }
        return builder.toString();
    }

    public static JsonNode getJsonNode(String value) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readTree(value);
        } catch (Exception e) {
            log.error("JSON parse error: {}", value, e);
        }
        return null;
    }

    public static JsonNode getJsonNode(InputStream stream, String encoding) {
        ObjectMapper mapper = new ObjectMapper();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream,
                Charset.availableCharsets().getOrDefault(encoding, StandardCharsets.UTF_8)));
        try {
            return mapper.readTree(reader);
        } catch(Exception e) {
            log.error("JSON load error.", e);
        }
        return null;
    }

    public static JsonNode getJsonNode(String url, String encoding) {
        InputStream stream = getStreamFromUrl(url);
        return stream == null ? null : getJsonNode(stream, encoding);
    }

    public static <T> T jsonToValue(String jsonString, Class<T> value) {
        JsonNode node = getJsonNode(jsonString);
        return jsonToValue(node, value);
    }

    public static <T> T jsonToValue(JsonNode node, Class<T> value) {
        if (node == null || value == null) return null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.treeToValue(node, value);
        } catch (Exception e) {
            log.warn("Error on parsing object {}", value.getName(), e);
        }
        return null;
    }

    public static <T> T jsonToValue(String url, String encoding, Class<T> value) {
        JsonNode node = getJsonNode(url, encoding);
        return jsonToValue(node, value);
    }

    public static <T> T jsonToValue(InputStream stream, String encoding, Class<T> value) {
        JsonNode node = getJsonNode(stream, encoding);
        return jsonToValue(node, value);
    }

    public static String objectToJsonString(Object object) {
        ObjectWriter writer = new ObjectMapper().writer(outputDateTimeFormat).withDefaultPrettyPrinter();
        try {
            String res = writer.writeValueAsString(object);
            return res;
        } catch (Exception e) {
            log.warn("Can't write object to JSON", e);
        }
        return "";
    }

    public static File checkFolder(String folder) {
        File f = new File(folder);
        if (!f.exists()) {
            if (f.mkdirs()) return f;
        } else return f;
        return null;
    }

    public static boolean streamCopy(InputStream source, OutputStream target) {
        byte[] buf = new byte[8192];
        int length;
        try {
            while ((length = source.read(buf)) > 0) {
                target.write(buf, 0, length);
            }
        } catch (Exception e) {
            log.warn("Can't save file to disk.", e);
            return false;
        }
        return true;
    }

    public static SimpleMessageListenerContainer createRabbitMQContainer(ConnectionFactory connectionFactory, String queueName, Object receiver) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName);
        container.setMessageListener(new MessageListenerAdapter(receiver, "receiveMessage"));
        return container;
    }

    public static void startRabbitMQContainer(ConnectionFactory connectionFactory, String queueName, Object receiver) {
        SimpleMessageListenerContainer container = createRabbitMQContainer(connectionFactory, queueName, receiver);
        container.start();
    }
}
