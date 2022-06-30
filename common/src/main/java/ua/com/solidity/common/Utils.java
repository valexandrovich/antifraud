package ua.com.solidity.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ShutdownSignalException;
import lombok.CustomLog;
import lombok.Getter;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


@CustomLog
@SuppressWarnings("unused")
public class Utils {
    @SuppressWarnings("SpellCheckingInspection")
    public static final String OUTPUT_DATETIME_FORMAT = "yyyy-MM-dd'T'hh:mm:ss.SSS[XXX]";
    public static final String OUTPUT_DATE_FORMAT = "yyyy-MM-dd[XXX]";
    public static final String OUTPUT_TIME_FORMAT = "hh:mm:ss.SSS[XXX]";
    private static final String RABBITMQ_LOG_ERROR_WITH_MESSAGE = "sendRabbitMQMessage: {} . ({}:{}) : {}";
    private static final String RABBITMQ_LOG_ERROR = "sendRabbitMQMessage: {} . ({}:{})";
    private static final long TRY_TO_SEND_DELTA = 180000; // 3 min
    private static final Timer timer = new Timer("Utils.timer");
    private static ApplicationContext context = null;
    private static com.rabbitmq.client.ConnectionFactory factory;
    private static Connection connection;
    private static Channel channel;
    private static ObjectMapper sortedMapper = null;
    private static XmlMapper xmlSortedMapper = null;
    private static final char[] hexChars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final String HEX_DIGITS = new String(hexChars);

    private static final Set<String> topics = new HashSet<>();
    private static final InternalApplicationContextLookup contextLookup = new InternalApplicationContextLookup();

    private static class InternalApplicationContextLookup {
        @Autowired
        ApplicationContext context;
    }

    private static class DeferredExecutionTimerTask extends TimerTask {
        Runnable runnable;
        public DeferredExecutionTimerTask(Runnable runnable) {
            this.runnable = runnable;
        }
        @Override
        public void run() {
            runnable.run();
        }
    }

    public interface PeriodicTask {
        void execute(TimerTask task);
    }

    @Getter
    private static class PeriodicExecutionTask extends TimerTask {
        private final PeriodicTask task;
        private final long taskPeriod;

        public PeriodicExecutionTask(PeriodicTask task, long taskPeriod) {
            this.task = task;
            this.taskPeriod = taskPeriod;
        }
        @Override
        public void run() {
            task.execute(this);
        }
    }

    private static class LimitedInputStream extends InputStream {
        private static final String ERR_MSG = "Unexpected end of stream.";
        private long size;
        private long pos = 0;
        private InputStream stream;
        private LimitedInputStream(InputStream stream, long size) {
            this.size = size;
            this.stream = stream;
        }

        public static InputStream createFromFile(File file) {
            if (file == null) return null;
            try(InputStream res = new FileInputStream(file)) {
                return new LimitedInputStream(res, file.length());
            } catch(Exception e) {
                // nothing
            }
            return null;
        }

        public static InputStream createFromUrl(String url) {
            URL u;
            try {
                u = new URL(url);
                return createFromUrl(u);
            } catch (Exception e) {
                // nothing
            }
            return null;
        }

        public static InputStream createFromUrl(URL url) {
            long size = -1;
            InputStream stream = null;
            try {
                URLConnection conn = url.openConnection();
                size = conn.getContentLengthLong();
                stream = conn.getInputStream();
            } catch (Exception e) {
                // nothing
            }
            return stream != null && size >= 0 ? new LimitedInputStream(stream, size) : null;
        }

        @Override
        public int read() throws IOException {
            int value = stream.read();
            if (value < 0 && pos != size) {
                throw new IOException(ERR_MSG);
            }
            ++pos;
            return value;
        }

        @Override
        public int read(@NonNull byte[] b, int off, int len) throws IOException {
            int count = stream.read(b, off, len);
            if (len > 0 && count == 0 && pos != size) {
                throw new IOException(ERR_MSG);
            }
            pos += count;
            return count;
        }

        @Override
        public void close() {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                    // nothing
                }
                stream = null;
            }
            size = pos = -1;
        }
    }

    private Utils() {
        //nothing
    }

    public static void setApplicationContext(ApplicationContext context) {
        if (Utils.context != null) return;
        Utils.context = context;
    }

    public static boolean checkApplicationContext() {
        if (context != null) return true;
        if (contextLookup.context != null) {
            context = contextLookup.context;
            return true;
        }
        log.error("Utils ApplicationContext not assigned. Use Utils.setApplicationContext(ApplicationContext) before.");
        return false;
    }

    public static ApplicationContext getApplicationContext() {
        return context;
    }

    public static String bytesToHex(byte[] bytes) {
        if (bytes != null) {
            char[] chars = new char[bytes.length * 2];
            int j = 0;
            for (byte b : bytes) {
                int v = Byte.toUnsignedInt(b);
                chars[j++] = hexChars[v >> 4];
                chars[j++] = hexChars[v & 0x0f];
            }
            return String.valueOf(chars);
        }
        return null;
    }

    public static byte[] hexToBytes(String str) {
        if (str != null) {
            str = str.toUpperCase();
            if ((str.length() % 2) != 0) str = '0' + str;
            byte[] res = new byte[str.length() / 2];
            int charIndex = 0;
            boolean failed = false;
            for (int i = 0; i < res.length; ++i) {
                int idxHi = HEX_DIGITS.indexOf(str.charAt(charIndex++));
                int idxLo = HEX_DIGITS.indexOf(str.charAt(charIndex++));
                if (idxHi < 0 || idxLo < 0) {
                    failed = true;
                    break;
                }
                res[i] = (byte)((idxHi << 4) | idxLo);
            }
            if (!failed) return res;
        }
        return new byte[0];
    }

    @SuppressWarnings("unused")
    public static <T> T saveEntity(T entity, Class<? extends JpaRepository<T, ?>> repositoryType) {
        if (checkApplicationContext()) {
            JpaRepository<T, ?> repository = context.getBean(repositoryType);
            try {
                return repository.save(entity);
            } catch (Exception e) {
                log.error("Error on save data.", e);
            }
        }
        return null;
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

    public static String getFileExtension(String fileName) {
        int pos = fileName.lastIndexOf('.');
        String res = pos >= 0 && pos < fileName.length() - 1 ? fileName.substring(pos + 1) : "";
        return res.indexOf('/') >= 0 || res.indexOf('\\') >= 0 ? "" : res;
    }

    public static InputStream getStreamFromUrl(String url) {
        InputStream stream = LimitedInputStream.createFromUrl(url);
        if (stream == null) {
            log.error("Can't read url {}", url);
        }
        return stream;
    }

    public static JsonNode getJsonNode(Object value) {
        ObjectMapper mapper = getSortedMapper();
        try {
            return mapper.valueToTree(value);
        } catch (Exception e) {
            log.error("JSON convert object to jsonNode error: {}", value, e);
        }
        return null;
    }

    public static JsonNode getJsonNode(String value) {
        ObjectMapper mapper = getSortedMapper();
        try {
            return mapper.readTree(value);
        } catch (Exception e) {
            log.error("JSON parse error: {}", value, e);
        }
        return null;
    }

    public static JsonNode getJsonNode(InputStream stream, String encoding) {
        ObjectMapper mapper = getSortedMapper();
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
            ObjectMapper mapper = getSortedMapper();
            try {
                return mapper.treeToValue(node, value);
            } catch (Exception e) {
                log.warn("Error on parsing object {}", value.getName(), e);
            }
        }
        return def;
    }

    private static DateTimeFormatter getOutputDateTimeFormat() {
        return DateTimeFormatter.ofPattern(OUTPUT_DATETIME_FORMAT);
    }

    private static DateTimeFormatter getOutputDateFormat() {
        return DateTimeFormatter.ofPattern(OUTPUT_DATE_FORMAT);
    }

    private static DateTimeFormatter getOutputTimeFormat() {
        return DateTimeFormatter.ofPattern(OUTPUT_TIME_FORMAT);
    }

    private static SimpleDateFormat getJsonOutputDateTimeFormat() {
        return new SimpleDateFormat(OUTPUT_DATETIME_FORMAT);
    }

    public static String localTimeToString(LocalTime time) {
        return time == null ? null : time.format(getOutputTimeFormat());
    }

    public static String localDateToString(LocalDate date) {
        return date == null ? null : date.format(getOutputDateFormat());
    }

    public static String zonedDateToString(ZonedDateTime datetime) {
        return datetime == null ? null : datetime.format(getOutputDateFormat());
    }

    public static String zonedDateTimeToString(ZonedDateTime datetime) {
        return datetime.format(getOutputDateTimeFormat());
    }

    public static String objectToJsonString(Object object) {
        ObjectWriter writer = getSortedMapper().writer().withDefaultPrettyPrinter();
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
        return streamCopy(source, target, null);
    }

    public static boolean streamCopy(InputStream source, OutputStream target, StatusChanger status) {
        byte[] buf = new byte[8192];
        int length;
        try {
            while ((length = source.read(buf)) > 0) {
                target.write(buf, 0, length);
                if (status != null) {
                    status.addProcessedVolume(length);
                }
            }
        } catch (Exception e) {
            log.error("Error due stream copy", e);
            return false;
        }
        return true;
    }

    public static synchronized String getContextProperty(String name, String defaultValue) {
        return checkApplicationContext() ? context.getEnvironment().getProperty(name, defaultValue) : defaultValue;
    }

    static synchronized void getRabbitMQConnectionFactory() {
        if (factory == null && checkApplicationContext()) {
            factory = new com.rabbitmq.client.ConnectionFactory();
            factory.setHost(context.getEnvironment().getProperty("spring.rabbitmq.host", "localhost"));
            factory.setPort(Integer.parseInt(context.getEnvironment().getProperty("spring.rabbitmq.port", "5672")));
            factory.setUsername(context.getEnvironment().getProperty("spring.rabbitmq.username", "guest"));
            factory.setPassword(context.getEnvironment().getProperty("spring.rabbitmq.password", "guest"));
            factory.setAutomaticRecoveryEnabled(true);
            factory.setNetworkRecoveryInterval(3000);
        }
    }

    private static void connectionShutdownListener(ShutdownSignalException e) {
        if (e != null) {
            log.debug("(Utils) RabbitMQ connection error.", e);
        }
        connection = null;
    }

    static synchronized void getRabbitMQConnection() {
        getRabbitMQConnectionFactory();
        if ((connection == null || !connection.isOpen()) && factory != null) {
            try {
                connection = factory.newConnection();
                connection.addShutdownListener(Utils::connectionShutdownListener);
            } catch (Exception e) {
                log.error("RabbitMQ Connection creation error.", e);
            }
        }
    }

    static synchronized Channel createRabbitMQChannel() {
        Channel channel = null;
        getRabbitMQConnection();
        if (connection != null) {
            try {
                channel = connection.createChannel();
                channel.addShutdownListener(Utils::channelShutdownListener);
            } catch (Exception e) {
                log.error("RabbitMQ Channel creation error.", e);
            }
        }
        return channel;
    }

     private static void channelShutdownListener(ShutdownSignalException e) {
        if (e != null) {
            log.debug("(Utils) RabbitMQ channel error.", e);
        }
        channel = null;
    }

    private static synchronized boolean channelNeeded() {
        if (channel == null || ! channel.isOpen()) channel = createRabbitMQChannel();
        return channel != null;
    }

    private static synchronized void rabbitMQLogError(String header, String topic, String routingKey, String message, Exception e) {
        if (message != null) {
            if (e != null) {
                log.error(RABBITMQ_LOG_ERROR_WITH_MESSAGE, header, topic, routingKey, message, e);
            } else {
                log.error(RABBITMQ_LOG_ERROR_WITH_MESSAGE, header, topic, routingKey, message);
            }
        } else {
            if (e != null) {
                log.error(RABBITMQ_LOG_ERROR, header, topic, routingKey, e);
            } else {
                log.error(RABBITMQ_LOG_ERROR, header, topic, routingKey);
            }
        }
    }

    static synchronized boolean prepareRabbitMQQueue(String queue) {
        if (channelNeeded()) {
            if (topics.contains(queue)) return true;
            try {
                channel.exchangeDeclare(queue, BuiltinExchangeType.TOPIC, true);
                channel.queueDeclare(queue, true, false, false, null);
                channel.queueBind(queue, queue, queue);
                topics.add(queue);
                return true;
            } catch (Exception e) {
                rabbitMQLogError("Exchange creation failed", queue, queue, null, e);
                return false;
            }
        }
        return false;
    }

    public static synchronized void sendRabbitMQMessage(String queue, String message) {
        if (queue == null || queue.isBlank()) return;
        if (prepareRabbitMQQueue(queue)) {
            try {
                channel.basicPublish(queue, queue, null, (message == null ? "" : message).getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                rabbitMQLogError("Can't send a message", queue, queue, message, e);
            }
        } else {
            rabbitMQLogError("Can't send a message now, waiting.", queue, queue, message, null);
            deferredExecute(TRY_TO_SEND_DELTA, ()-> sendRabbitMQMessage(queue, message));
        }
    }

    @SuppressWarnings("all")
    public static synchronized TimerTask deferredExecute(long milliseconds, Runnable runnable) {
        TimerTask timerTask = new DeferredExecutionTimerTask(runnable);
        timer.schedule(timerTask, milliseconds);
        return timerTask;
    }

    public static synchronized TimerTask periodicExecute(long milliseconds, PeriodicTask task) {
        TimerTask res = new PeriodicExecutionTask(task, milliseconds);
        timer.schedule(res, 0, milliseconds);
        return res;
    }

    public static synchronized long getPeriodicExecutionTaskPeriod(TimerTask task) {
        if (task instanceof PeriodicExecutionTask) {
            return ((PeriodicExecutionTask) task).taskPeriod;
        }
        return 0;
    }

    public static String getOutputFolder(String outputFolder, String defaultEnvironmentVariableForOutputFolder) {
        if ((outputFolder == null || outputFolder.length() == 0) &&
                defaultEnvironmentVariableForOutputFolder != null && defaultEnvironmentVariableForOutputFolder.length() > 0) {
            Map<String, String> map = System.getenv();
            outputFolder = map.getOrDefault(defaultEnvironmentVariableForOutputFolder, null);
            if (outputFolder == null) outputFolder = System.getProperty("java.io.tmpdir");
        }
        return outputFolder;
    }

    public static ZipEntry getZipEntry(ZipFile zipFile, String match) {
        List<ZipEntry> found = zipFile.stream().
                filter(zipEntry -> !zipEntry.isDirectory() && zipEntry.getName().matches(match)).
                collect(Collectors.toList());

        if (found.isEmpty()) {
            log.warn("No entries found in zip file for match {}", match);
            return null;
        }

        if (found.size() > 1) {
            log.warn("Too many items in zip file matches for {}, first selected.", match);
        }
        return found.get(0);
    }

    private static class SortingNodeFactory extends JsonNodeFactory {
        @Override
        public ObjectNode objectNode() {
            return new ObjectNode(this, new TreeMap<>());
        }
    }

    public static ObjectMapper getSortedMapper() {
        if (sortedMapper == null) {
            sortedMapper = JsonMapper.builder()
                    .nodeFactory(new SortingNodeFactory())
                    .build().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true).
                    configure(SerializationFeature.INDENT_OUTPUT, true).setDateFormat(getJsonOutputDateTimeFormat());
        }
        return sortedMapper;
    }

    public static XmlMapper getSortedXmlMapper() {
        if (xmlSortedMapper == null) {
            xmlSortedMapper = (XmlMapper) XmlMapper.xmlBuilder().
                    nodeFactory(new SortingNodeFactory()).
                    build().
                    configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true).
                    configure(SerializationFeature.INDENT_OUTPUT, true).setDateFormat(getJsonOutputDateTimeFormat());
        }
        return xmlSortedMapper;
    }

    public static JsonNode getSortedJsonNode(JsonNode node) {
        try {
            ObjectMapper sortedMapper = getSortedMapper();
            return sortedMapper.treeToValue(node, JsonNode.class);
        } catch (JsonProcessingException e) {
            log.error("Can't resort node.", e);
        }
        return null;
    }

    public static byte[] getJsonNodeBytes(JsonNode node) {
        try {
            ObjectMapper sortedMapper = getSortedMapper();
            return sortedMapper.writeValueAsBytes(node);
        } catch (JsonProcessingException e) {
            log.error("Can't convert node to bytes.", e);
        }
        return new byte[0];
    }

    public static String getSortedJsonNodeString(JsonNode node) {
        try {
            ObjectMapper sortedMapper = getSortedMapper();
            return sortedMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            log.error("Can't convert node to Sorted String.", e);
        }
        return null;
    }

    public static byte[] complexDigest(JsonNode node) {
        if (node != null) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA3-384");
                byte[] input = getJsonNodeBytes(node);
                return md.digest(input);
            } catch (Exception e) {
                log.error("ComplexDigest error.", e);
            }
        }
        return new byte[0];
    }

    @SuppressWarnings("unused")
    public static String messageFormat(String pattern, Object ... args) {
        return MessageFormatter.arrayFormat(pattern, args).getMessage();
    }

    @SuppressWarnings("unused")
    public static boolean registerActions(Class<? extends ActionObject> clazz, String ...actions) {
        boolean res = clazz == null || actions.length == 0;
        if (res) ActionObject.register(clazz, actions);
        return res;
    }

    @SuppressWarnings("unused")
    public static ActionObject getActionObject(JsonNode node) {
        return ActionObject.getAction(node);
    }

    @SuppressWarnings("unsed")
    public static ActionObject getActionObject(String json) {
        return ActionObject.getAction(json);
    }
}
