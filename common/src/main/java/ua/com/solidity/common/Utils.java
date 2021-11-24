package ua.com.solidity.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
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

@Slf4j
public class Utils {
    public static final String OUTPUT_DATETIME_FORMAT = "yyyy-MM-dd'T'hh:mm:ss.SSSXXX";

    private Utils() {
        //nothing
    }

    public static DateFormat outputDateTimeFormat() {
        return new SimpleDateFormat(OUTPUT_DATETIME_FORMAT);
    }

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

    public static JsonNode getJsonNode(Object value) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.valueToTree(value);
        } catch (Exception e) {
            log.error("JSON convert object to jsonNode error: {}", value, e);
        }
        return null;
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
        return jsonToValueDef(node, value, null);
    }

    public static <T> T jsonToValueDef(JsonNode node, Class<T> value, T def) {
        if (node != null && value != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.treeToValue(node, value);
            } catch (Exception e) {
                log.warn("Error on parsing object {}", value.getName(), e);
            }
        }
        return def;
    }

    public static String objectToJsonString(Object object) {
        ObjectWriter writer = new ObjectMapper().writer(outputDateTimeFormat()).withDefaultPrettyPrinter();
        try {
            return writer.writeValueAsString(object);
        } catch (Exception e) {
            log.warn("Can't write object to JSON", e);
        }
        return "";
    }

    @SuppressWarnings("unused")
    public static JsonNode getNodeValue(JsonNode node, String name) {
        return node.has(name) ? node.get(name) : JsonNodeFactory.instance.nullNode();
    }

    public static <T> T getNodeValue(JsonNode node, String name, Class<T> clazz, T defaultValue) {
        if (node == null || name == null || name.length() == 0 || !node.hasNonNull(name)) return defaultValue;
        return jsonToValueDef(node.get(name), clazz, defaultValue);
    }

    public static <T> T getNodeValue(JsonNode node, String name, Class<T> clazz) {
        return getNodeValue(node, name, clazz,null);
    }

    public static <T> T getNodeValue(JsonNode node, int index, Class<T> clazz, T defaultValue) {
        if (node == null || index < 0 || index >= node.size() || !node.hasNonNull(index)) return defaultValue;
        return jsonToValueDef(node.get(index), clazz, defaultValue);
    }

    @SuppressWarnings("unused")
    public static <T> T getNodeValue(JsonNode node, int index, Class<T> clazz) {
        return getNodeValue(node, index, clazz, null);
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
