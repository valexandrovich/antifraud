package ua.com.solidity.common;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ShutdownSignalException;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;


@CustomLog
@SuppressWarnings("unused")
public class Utils {
    @SuppressWarnings("SpellCheckingInspection")
    public static final String OUTPUT_DATETIME_FORMAT = "yyyy-MM-dd'T'hh:mm:ss.SSS[XXX]";
    public static final String OUTPUT_DATE_FORMAT = "yyyy-MM-dd[XXX]";
    public static final String OUTPUT_TIME_FORMAT = "hh:mm:ss.SSS[XXX]";
    @Getter
    @Setter
    private static int prettyIndentSize = 4;
    @Getter
    @Setter
    private static boolean prettyArrayIndentation = true;
    private static final String RABBITMQ_LOG_ERROR_WITH_MESSAGE = "sendRabbitMQMessage: {} . ({}:{}) : {}";
    private static final String RABBITMQ_LOG_ERROR = "sendRabbitMQMessage: {} . ({}:{})";
    private static final String NFS_VARIABLE_PROPERTY = "otp.nfs.variable";
    private static final String NFS_FOLDER_PROPERTY = "otp.nfs.folder";
    private static final String DEFAULT_NFS_VARIABLE = "OTP_TEMP";
    private static final long TRY_TO_SEND_DELTA = 180000; // 3 min
    private static final Timer TIMER = new Timer("Utils.timer");
    private static final Map<String, Object> STORED_PROPERTIES = new HashMap<>();
    private static ApplicationContext context = null;
    private static com.rabbitmq.client.ConnectionFactory factory;
    private static Connection connection;
    private static Channel channel;
    private static ObjectMapper sortedMapper = null;
    private static XmlMapper xmlSortedMapper = null;
    private static final Map<Integer, DefaultPrettyPrinter> PRINTER_CACHE = new HashMap<>();
    private static ObjectMapper prettyMapper = null;

    private static final char[] HEX_CHARS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final String HEX_DIGITS = new String(HEX_CHARS);

    private static final Set<String> TOPICS = new HashSet<>();

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
        private static final String ERR_MSG = "Unexpected end of stream";
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
            } catch(FileNotFoundException e) {
                log.error("[createFromFile] File not found: {}", file);
            } catch(IOException e) {
                log.error("[createFromFile] IO Exception: {}", e.getMessage());
            }
            return null;
        }

        public static InputStream createFromUrl(String url) {
            URL u;
            try {
                u = new URL(url);
                return createFromUrl(u);
            } catch (MalformedURLException e) {
                log.error("[createFromUrl] Malformed URL: {}", url);
            }
            return null;
        }

        public static InputStream createFromUrl(URL url) {
            long size = -1;
            InputStream stream = null;
            try {

                URL utest = new URL("https://data.gov.ua");
                URLConnection ctest = utest.openConnection();
                InputStream streamTest = ctest.getInputStream();
                System.out.println("Test connection length: " + ctest.getContentLengthLong());
                System.out.println("Test connection timeout: " + ctest.getContent());

                URLConnection conn = url.openConnection();
                size = conn.getContentLengthLong();
                stream = conn.getInputStream();
            } catch (IOException e) {
                log.error("[createFromUrl] IO Exception: {}", e.getMessage());
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
                } catch (IOException e) {
                    log.error("[close] Cannot close stream IO Exception: {}", e.getMessage());
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
        OtpExchange.prepareSpecialQueues();
    }

    public static boolean checkApplicationContext() {
        if (context != null) return true;
        log.error("[checkApplicationContext] Utils ApplicationContext not assigned. Use Utils.setApplicationContext(ApplicationContext) before");
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
                chars[j++] = HEX_CHARS[v >> 4];
                chars[j++] = HEX_CHARS[v & 0x0f];
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
            return repository.save(entity);
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
            log.error("[getStreamFromUrl] Can't read from url {}", url);
        }
        return stream;
    }

    public static JsonNode getJsonNode(Object value) {
        ObjectMapper mapper = getSortedMapper();
        try {
            return mapper.valueToTree(value);
        } catch (IllegalArgumentException e) {
            log.error("[getJsonNode] JSON convert object to jsonNode error: {}", value, e);
        }
        return null;
    }

    public static JsonNode getJsonNode(String value) {
        ObjectMapper mapper = getSortedMapper();
        try {
            return mapper.readTree(value);
        } catch (JsonProcessingException e) {
            log.error("[getJsonNode] JSON processing error: {}", value, e);
        }
        return null;
    }

    public static JsonNode getJsonNode(InputStream stream, String encoding) {
        ObjectMapper mapper = getSortedMapper();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream,
                Charset.availableCharsets().getOrDefault(encoding, StandardCharsets.UTF_8)));
        try {
            return mapper.readTree(reader);
        } catch(IOException e) {
            log.error("[getJsonNode] JSON load error", e);
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
            } catch (JsonProcessingException e) {
                log.warn("[jsonToValue] Error on parsing object {}", value.getName(), e);
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
        } catch (JsonProcessingException e) {
            log.warn("[objectToJsonString] Can't write object to JSON", e);
        }
        return "";
    }

    public static JsonNode objectToJsonNode(ObjectMapper mapper, Object object) {
        if (mapper == null || object == null) return null;
        JsonNode res = null;
        try {
            res = mapper.valueToTree(object);
        } catch (IllegalArgumentException e) {
            log.error("[objectToJsonNode] Can't convert object to JsonNode", e);
        }
        return res;
    }

    public static JsonNode objectToJsonNode(Object object) {
        return objectToJsonNode(getSortedMapper(), object);
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
        Path p = Path.of(folder);
        try {
            Files.createDirectories(p);
        } catch (IOException e) {
            log.debug("[checkFolder] Can't create folder: {}", folder);
            return null;
        }

        return new File(folder);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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
        } catch (IOException e) {
            log.error("[streamCopy] Error during stream copy", e);
            return false;
        }
        return true;
    }

    public static synchronized String getContextProperty(String name, String defaultValue, boolean contextOnly) {
        if (!contextOnly && STORED_PROPERTIES.containsKey(name)) {
            Object value = STORED_PROPERTIES.get(name);
            return value == null ? defaultValue : value.toString();
        }
        return checkApplicationContext() ? context.getEnvironment().getProperty(name, defaultValue) : defaultValue;
    }

    public static String getContextProperty(String name, String defaultValue) {
        return getContextProperty(name, defaultValue, false);
    }

    public static long getLongContextProperty(String name, long defaultValue, boolean contextOnly) {
        String value = getContextProperty(name, String.valueOf(defaultValue), contextOnly).trim();
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.error("[getLongContextProperty] Error parsing long {}", value);
        }
        return defaultValue;
    }

    public static long getLongContextProperty(String name, long defaultValue) {
        return getLongContextProperty(name, defaultValue, false);
    }

    public static int getIntContextProperty(String name, int defaultValue, boolean contextOnly) {
        long value = getLongContextProperty(name, defaultValue, contextOnly);
        return value < Integer.MIN_VALUE || value > Integer.MAX_VALUE ? defaultValue : (int) value;
    }

    public static int getIntContextProperty(String name, int defaultValue) {
        return getIntContextProperty(name, defaultValue, false);
    }

    public static boolean getBooleanContextProperty(String name, boolean defaultValue, boolean contextOnly) {
        String value = getContextProperty(name, String.valueOf(defaultValue), contextOnly).trim();
        return value.equalsIgnoreCase("true");
    }

    public static boolean getBooleanContextProperty(String name, boolean defaultValue) {
        return getBooleanContextProperty(name, defaultValue, false);
    }

    public static double getFloatContextProperty(String name, double defaultValue, boolean contextOnly) {
        String value = getContextProperty(name, String.valueOf(defaultValue), contextOnly);
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.error("[getFloatContextProperty] Error parsing double {}", value);
        }
        return defaultValue;
    }

    public static double getFloatContextProperty(String name, double defaultValue) {
        return getFloatContextProperty(name, defaultValue, false);
    }

    public static synchronized void setContextProperty(String name, Object value, boolean ifNotExists) {
        if (ifNotExists) {
            String v = getContextProperty(name, null);
            if (v != null && !v.isBlank()) return;
        }

        if (value == null || (value instanceof String && ((String) value).isBlank())) {
            STORED_PROPERTIES.remove(name);
        } else {
            STORED_PROPERTIES.put(name, value);
        }
    }

    public static void setContextProperty(String name, Object value) {
        setContextProperty(name, value, false);
    }

    static synchronized void getRabbitMQConnectionFactory() {
        if (factory == null) {
            factory = new com.rabbitmq.client.ConnectionFactory();
            factory.setHost(getContextProperty("spring.rabbitmq.host", "localhost"));
            factory.setPort(Integer.parseInt(getContextProperty("spring.rabbitmq.port", "5672")));
            factory.setUsername(getContextProperty("spring.rabbitmq.username", "guest"));
            factory.setPassword(getContextProperty("spring.rabbitmq.password", "guest"));
            factory.setAutomaticRecoveryEnabled(true);
            factory.setNetworkRecoveryInterval(3000);
        }
    }

    private static void connectionShutdownListener(ShutdownSignalException e) {
        if (e != null) {
            log.debug("[connectionShutdownListener] (Utils) RabbitMQ connection error", e);
        }
        connection = null;
    }

    static synchronized void getRabbitMQConnection() {
        getRabbitMQConnectionFactory();
        if ((connection == null || !connection.isOpen()) && factory != null) {
            try {
                connection = factory.newConnection();
                connection.addShutdownListener(Utils::connectionShutdownListener);
            } catch (IOException e) {
                log.error("[getRabbitMQConnection] RabbitMQ IO Connection creation error", e);
            }
            catch (TimeoutException e) {
                log.error("[getRabbitMQConnection] RabbitMQ Timeout Connection creation error", e);
            }
        }
    }

    static synchronized Channel createRabbitMQChannel() {
        Channel res = null;
        getRabbitMQConnection();
        if (connection != null) {
            try {
                res = connection.createChannel();
                res.addShutdownListener(Utils::channelShutdownListener);
            } catch (IOException e) {
                log.error("[createRabbitMQChannel] RabbitMQ Channel creation error", e);
            }
        }
        return res;
    }

     private static void channelShutdownListener(ShutdownSignalException e) {
        if (e != null) {
            log.debug("[channelShutdownListener] (Utils) RabbitMQ channel error", e);
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

    private static boolean queueDeclare(String queue) {
        Map<String, Object> params = OtpExchange.getQueueParams(queue);
        if (channelNeeded()) {
            try {
                channel.queueDeclare(queue, true, false, false, params);
            } catch (IOException e) {
                if (channelNeeded()) {
                    try {
                        channel.queuePurge(queue); // no messages
                        channel.queueDelete(queue);
                        channel.queueDeclare(queue, true, false, false, params);
                    } catch (IOException e1) {
                        channelNeeded();
                        return false;
                    }
                }
            }
            try {
                channel.queueBind(queue, queue, queue);
            } catch (IOException e) {
                log.debug("[queueDeclare] Channel is needed");
                channelNeeded();
                return false;
            }
        }
        return true;
    }

    public static synchronized boolean prepareRabbitMQQueue(String queue) {
        if (channelNeeded()) {
            if (TOPICS.contains(queue)) return true;
            try {
                channel.exchangeDeclare(queue, BuiltinExchangeType.TOPIC, true);
                if (!queueDeclare(queue)) return false;
                TOPICS.add(queue);
                return true;
            } catch (IOException e) {
                rabbitMQLogError("[prepareRabbitMQQueue] Exchange creation failed", queue, queue, null, e);
                return false;
            }
        }
        return false;
    }

    public static synchronized void sendRabbitMQMessage(String queue, Object obj, byte priority) {
        if (queue == null || queue.isBlank()) return;
        String message = obj instanceof String ? (String) obj : objectToJsonString(obj);
        if (prepareRabbitMQQueue(queue)) {
            try {
                AMQP.BasicProperties props = null;
                if (priority > 0) {
                    props = new AMQP.BasicProperties.Builder().priority((int) priority).build();
                }
                channel.basicPublish(queue, queue, props, (message == null ? "" : message).getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                rabbitMQLogError("[sendRabbitMQMessage] Can't send a message", queue, queue, message, e);
            }
        } else {
            rabbitMQLogError("[sendRabbitMQMessage] Can't send a message now, waiting", queue, queue, message, null);
            deferredExecute(TRY_TO_SEND_DELTA, ()-> sendRabbitMQMessage(queue, message));
        }
    }

    public static synchronized void sendRabbitMQMessage(String queue, Object obj) {
        sendRabbitMQMessage(queue, obj, (byte) 0);
    }

    @SuppressWarnings("all")
    public static synchronized TimerTask deferredExecute(long milliseconds, Runnable runnable) {
        TimerTask timerTask = new DeferredExecutionTimerTask(runnable);
        TIMER.schedule(timerTask, milliseconds);
        return timerTask;
    }

    public static synchronized TimerTask periodicExecute(long milliseconds, PeriodicTask task) {
        TimerTask res = new PeriodicExecutionTask(task, milliseconds);
        TIMER.schedule(res, 0, milliseconds);
        return res;
    }

    public static synchronized long getPeriodicExecutionTaskPeriod(TimerTask task) {
        if (task instanceof PeriodicExecutionTask) {
            return ((PeriodicExecutionTask) task).taskPeriod;
        }
        return 0;
    }

    public static String normalizePath(String folder) {
        if (folder != null && !folder.isBlank()) {
            folder = folder.trim().replace('\\', '/');
            if (folder.endsWith("/")) {
                folder = folder.substring(0, folder.length() - 1);
            }
            return folder;
        }
        return null;
    }

    public static String getOutputFolder(String outputFolder, String defaultEnvironmentVariableForOutputFolder) {
        if ((outputFolder == null || outputFolder.isBlank()) &&
                defaultEnvironmentVariableForOutputFolder != null && !defaultEnvironmentVariableForOutputFolder.isBlank()) {
            Map<String, String> map = System.getenv();
            outputFolder = map.getOrDefault(defaultEnvironmentVariableForOutputFolder, null);
            if (outputFolder == null) outputFolder = System.getProperty("java.io.tmpdir");
        }
        return outputFolder;
    }

    public static String getOutputFolder(String outputFolder, String defaultEnvironmentVariableForOutputFolder, String localPath) {
        String folder = normalizePath(getOutputFolder(outputFolder, defaultEnvironmentVariableForOutputFolder));
        if (folder == null) return null;
        localPath = normalizePath(localPath);
        if (localPath != null) {
            folder = normalizePath(folder) + "/" + normalizePath(localPath);
        }
        return folder;
    }

    public static File prepareOutputFolder(String outputFolder, String defaultEnvironmentVariableForOutputFolder, String localPath) {
        return checkFolder(getOutputFolder(outputFolder, defaultEnvironmentVariableForOutputFolder, localPath));
    }

    public static File prepareOutputFolder(String outputFolder, String defaultEnvironmentVariableForOutputFolder) {
        return prepareOutputFolder(outputFolder, defaultEnvironmentVariableForOutputFolder, null);
    }

    // bind deprecated application properties to otp.nfs.folder, otp.nfs.variable values

    public static void bindNFSProperties(String folderValue, String variableValue) {
        if (folderValue != null && !folderValue.isBlank()) {
            setContextProperty(NFS_FOLDER_PROPERTY, folderValue);
        }

        if (variableValue != null && !variableValue.isBlank()) {
            setContextProperty(NFS_VARIABLE_PROPERTY, variableValue);
        }
    }

    public static String getNFSVariable() {
        return getContextProperty(NFS_VARIABLE_PROPERTY, DEFAULT_NFS_VARIABLE);
    }

    public static String getNFSFolder(String localPath) {
        String folder = getContextProperty(NFS_FOLDER_PROPERTY, null);
        String variable = getNFSVariable();
        return getOutputFolder(folder, variable, localPath);
    }

    public static String getNFSFolder() {
        return getNFSFolder(null);
    }

    public static File prepareNFSFolder(String localPath) {
        String folder = getContextProperty(NFS_FOLDER_PROPERTY, null);
        String variable = getContextProperty(NFS_VARIABLE_PROPERTY, DEFAULT_NFS_VARIABLE);
        return prepareOutputFolder(folder, variable, localPath);
    }

    public static File prepareNFSFolder() {
        return prepareNFSFolder(null);
    }

    public static File getFileFromNFSFolder(String fileName, boolean forRead) {
        Path p = Path.of(fileName);
        File res;
        Path parent = p.getParent();
        if (!p.isAbsolute()) {
            parent = Path.of(getNFSFolder(parent == null ? null : parent.toString()));
            p = parent.resolve(p.getFileName());
        }

        res = p.toFile();

        if (forRead && !res.exists()) {
            return null;
        } else {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                log.debug("[getFileFromNFSFolder] Cannot create folder {}", parent);
                return null;
            }
        }
        return res;
    }

    public static File checkFolder(String folder, boolean useNFS) {
        if (!useNFS) {
            return checkFolder(folder);
        } else {
            Path parent = Path.of(folder);
            if (!parent.isAbsolute()) {
                parent = Path.of(getNFSFolder(folder));
            }
            try {
                Files.createDirectories(parent);
            } catch(IOException e) {
                log.debug("[checkFolder] Cannot create folder {}", parent);
                return null;
            }
            return parent.toFile();
        }
    }

    public static File getFileFromFolder(File targetFolder, String localPath) {
        Path realPath = targetFolder.toPath().resolve(localPath);
        Path parent = realPath.getParent();
        try {
            Files.createDirectories(parent);
        } catch (IOException e) {
            log.debug("[getFileFromFolder] Cannot create folder {}", parent);
            return null;
        }
        return realPath.toFile();
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
            log.warn("Too many items in zip file matches for {}, first selected", match);
        }
        return found.get(0);
    }

    private static class SortingNodeFactory extends JsonNodeFactory {
        @Override
        public ObjectNode objectNode() {
            return new ObjectNode(this, new TreeMap<>());
        }
    }

    public static DefaultPrettyPrinter getPrettyPrinter(int indent, boolean arrayIndentation) {
        if (indent <= 0) {
            indent = 2;
        }

        int id = indent;

        if (arrayIndentation) {
            id = -indent;
        }

        DefaultPrettyPrinter printer = PRINTER_CACHE.getOrDefault(id, null);

        if (printer == null) {
            char[] indentArray = new char[indent];
            Arrays.fill(indentArray, ' ');
            DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter(String.copyValueOf(indentArray),
                    DefaultIndenter.SYS_LF);
            printer = new DefaultPrettyPrinter();
            printer.indentObjectsWith(indenter);

            if (arrayIndentation) {
                printer.indentArraysWith(indenter);
            }

            PRINTER_CACHE.put(id, printer);
        }

        return printer;
    }

    public static DefaultPrettyPrinter defaultPrettyPrinter() {
        return getPrettyPrinter(prettyIndentSize, prettyArrayIndentation);
    }

    public static ObjectMapper getPrettyMapper(DefaultPrettyPrinter printer) {
        if (prettyMapper == null) {
            prettyMapper = JsonMapper.builder().
                    build().
                    configure(SerializationFeature.INDENT_OUTPUT, true).
                    setDateFormat(getJsonOutputDateTimeFormat());
        }

        prettyMapper.setDefaultPrettyPrinter(Objects.requireNonNullElseGet(printer, Utils::defaultPrettyPrinter));
        return prettyMapper;
    }

    public static ObjectMapper getPrettyMapper() {
        return getPrettyMapper(null);
    }

    public static ObjectMapper getPrettyMapper(int indent, boolean arrayIndentation) {
        return getPrettyMapper(getPrettyPrinter(indent, arrayIndentation));
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
            log.error("[getSortedJsonNode] Can't resort node", e);
        }
        return null;
    }

    public static byte[] getJsonNodeBytes(JsonNode node) {
        try {
            ObjectMapper sortedMapper = getSortedMapper();
            return sortedMapper.writeValueAsBytes(node);
        } catch (JsonProcessingException e) {
            log.error("[getJsonNodeBytes] Can't convert node to bytes", e);
        }
        return new byte[0];
    }

    public static String getJsonNodePrettyString(JsonNode node, DefaultPrettyPrinter printer) {
        ObjectMapper mapper = getPrettyMapper(printer);
        try {
            return mapper.writer().writeValueAsString(node);
        } catch (JsonProcessingException e) {
            log.debug("[getJsonNodePrettyString] Can't write value as string", e);
            return null;
        }
    }

    public static String getJsonNodePrettyString(JsonNode node, int indent, boolean arrayIndentation) {
        return getJsonNodePrettyString(node, getPrettyPrinter(indent, arrayIndentation));
    }

    public static String getJsonNodePrettyString(JsonNode node) {
        return getJsonNodePrettyString(node, null);
    }

    public static String getJsonNodeUniqueString(JsonNode node) {
        try {
            ObjectMapper sortedMapper = getSortedMapper();
            return sortedMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            log.error("[getJsonNodeUniqueString] Can't convert node to Sorted String", e);
        }
        return null;
    }

    public static boolean writeJsonNodeToFile(File file, JsonNode node, int indent, boolean arrayIndentation) {
        if (file == null || node == null) return false;
        try(Writer w = new FileWriterWithEncoding(file, StandardCharsets.UTF_8)) {
            w.write(getPrettyMapper(indent, arrayIndentation).writeValueAsString(node));
        } catch (IOException e) {
            log.error("[writeJsonNodeToFile] File {} not saved", file.getAbsolutePath());
            return false;
        }
        return true;
    }

    @SuppressWarnings("unused")
    public static boolean writeJsonNodeToFile(File file, JsonNode node) {
        return writeJsonNodeToFile(file, node, prettyIndentSize, prettyArrayIndentation);
    }

    public static byte[] complexDigest(JsonNode node) {
        if (node != null) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA3-384");
                byte[] input = getJsonNodeBytes(node);
                return md.digest(input);
            } catch (NoSuchAlgorithmException e) {
                log.error("[complexDigest] SHA3-384 algorithm is not supported", e);
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

    public static void waitMs(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static String getExceptionString(Exception e) {
        return getExceptionString(e, "/n");
    }

    public static String getExceptionString(Exception e, String delimiter) {
        StringBuilder builder = new StringBuilder();
        Throwable t = e;
        while (t != null) {
            if (builder.length() > 0) {
                builder.append(delimiter);
            }
            builder.append(messageFormat("{}:{}", t.getClass().getName(), t.getMessage()));
            t = t.getCause();
        }

        return builder.toString();
    }
}
