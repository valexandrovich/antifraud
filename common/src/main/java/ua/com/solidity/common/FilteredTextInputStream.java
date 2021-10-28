package ua.com.solidity.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Scanner;

@Slf4j
public class FilteredTextInputStream extends InputStream {
    private static final byte BYTE_LINE_FEED = 10;
    private static final byte BYTE_CARRIAGE_RETURN = 13;
    private static final String STR_PREFIX = "...";

    private enum CRLFState {
        NORMAL,
        CARRIAGE_RETURN,
        LINE_FEED
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class Location {
        private long line = 0;
        private long col = 0;
        private CRLFState state = CRLFState.NORMAL;
        private long position = 0;
        private int bufferPos = 0;
        private Buffer buffer = null;

        public Location(Location copy) {
            copyFrom(copy);
        }

        public final void copyFrom(Location location) {
            line = location.line;
            col = location.col;
            position = location.position;
            state = location.state;
        }

        private void newLine() {
            ++line;
            col = 0;
            state = CRLFState.NORMAL;
        }

        public final void pushByte(byte value) {
            boolean deferredNewLine = false;
            if (state == CRLFState.NORMAL) {
                if (value == BYTE_LINE_FEED) {
                    state = CRLFState.LINE_FEED;
                } else if (value == BYTE_CARRIAGE_RETURN) {
                    state = CRLFState.CARRIAGE_RETURN;
                }
            } else if ((state == CRLFState.CARRIAGE_RETURN && value == BYTE_LINE_FEED) ||
                    (state == CRLFState.LINE_FEED && value == BYTE_CARRIAGE_RETURN)) {
                deferredNewLine = true;
            } else {
                newLine();
            }
            ++position;
            ++bufferPos;
            if (deferredNewLine) {
                newLine();
            } else {
                ++col;
            }
        }
    }

    private static class LineStream extends ByteArrayInputStream {
        public LineStream(byte[] buf) {
            super(buf);
        }
        public final int position() {
            return pos;
        }
    }

    @Getter
    @Setter
    private static class LineBuffer {
        public final long line;
        public final long position;
        public final long col;
        public final byte[] bytes;
        private int size;
        public LineBuffer(long position, long line, long col, byte[] bytes) {
            this.position = position;
            this.line = line;
            this.col = col;
            this.bytes = bytes;
            this.size = this.bytes.length;
            while (size > 0 && (bytes[size - 1] == BYTE_LINE_FEED || bytes[size - 1] == BYTE_CARRIAGE_RETURN)) {
                size--;
            }
        }

        public final boolean hasPrefix() {
            return col > 0;
        }

        public final String getString(String encoding) {
            LineStream stream = new LineStream(bytes);
            InputStreamReader reader = new InputStreamReader(stream, Charset.availableCharsets().getOrDefault(encoding, StandardCharsets.UTF_8));
            Scanner scanner = new Scanner(reader);
            return scanner.nextLine();
        }

        public final String getPositionString(long charIndex) {
            int index = (int) (charIndex - col);
            return " ".repeat(index) + "^";
        }

        public final String getPositionString(long byteIndex, String encoding) {
            byteIndex -= col;
            StringBuilder builder = new StringBuilder();
            LineStream stream = new LineStream(bytes);
            InputStreamReader reader = new InputStreamReader(stream, Charset.availableCharsets().getOrDefault(encoding, StandardCharsets.UTF_8));
            try {
                while (stream.position() < byteIndex) {
                    //noinspection ResultOfMethodCallIgnored
                    reader.read();
                    if (stream.position() < byteIndex) builder.append(" ");
                }
                builder.append("^");
            } catch (Exception e) {
                log.warn("<INTERNAL> Illegal character found.");
            }
            return builder.toString();
        }
    }

    @Getter
    @Setter
    private static class Buffer {
        private final byte[] byteBuffer;
        private int size = 0;
        Location start = new Location();
        Location finish = new Location();

        public Buffer(int bufferSize) {
            byteBuffer = new byte[bufferSize];
            start.setBuffer(this);
            finish.setBuffer(this);
        }

        public final boolean full() {
            return size == byteBuffer.length;
        }

        public final void prepareNextBuffer(Buffer buf) {
            buf.size = 0;
            buf.start.copyFrom(finish);
            buf.start.setBufferPos(0);
            buf.finish.copyFrom(buf.start);
        }

        public final boolean loadBuffer(InputStream stream) {
            try {
                size = stream.read(byteBuffer);
                if (size > 0) {
                    for (int i = 0; i < size; ++i) {
                        int value = byteBuffer[i] & 0xff;
                        if (value < 32 && value != 9 && value != 10 && value != 13) byteBuffer[i] = 32;
                        finish.pushByte(byteBuffer[i]);
                    }
                }
                return size > 0;
            } catch (Exception e) {
                log.warn("Read error", e);
            }
            return false;
        }

        public final Location searchLine(long line) {
            if (start.line > line || finish.line < line) {
                return null;
            }
            Location res = new Location(start);
            for (int i = 0; i < size && res.line < line; ++i) {
                res.pushByte(byteBuffer[i]);
            }
            return res;
        }

        public final Location searchNextLine(Location location) {
            if (location == null || location.buffer != this) return null;
            Location res = new Location(location);
            for (int i = location.bufferPos; i < size && location.line == res.line; ++i) {
                res.pushByte(byteBuffer[i]);
            }
            return res;
        }
    }

    private final InputStream stream;
    private final Buffer[] buffers;
    private boolean hasPriorBuffer = false;
    private int currentBufferPosition = 0;

    public FilteredTextInputStream(InputStream stream, int bufferSize) {
        this.stream = stream;
        buffers = new Buffer[2];
        buffers[0] = new Buffer(bufferSize);
        buffers[1] = new Buffer(bufferSize);
        if (stream != null) getCurrentBuffer().loadBuffer(stream);
    }

    private boolean swapBuffers() {
        currentBufferPosition = 0;
        if (!buffers[1].full()) return false;
        Buffer temp = buffers[0];
        buffers[0] = buffers[1];
        buffers[1] = temp;
        buffers[0].prepareNextBuffer(temp);
        hasPriorBuffer = true;
        return (buffers[1].loadBuffer(stream));
    }

    protected final Buffer getCurrentBuffer() {
        return buffers[1];
    }

    public final long getPosition() {
        return getCurrentBuffer().start.getPosition() + currentBufferPosition;
    }

    private Location[] getLineLocations(long line) {
        Location[] locations = {null, null};
        int locationIndex = 0;
        Location location;
        if (hasPriorBuffer) {
            location = buffers[0].searchLine(line);
            if (location != null) locations[locationIndex++] = location;
        }
        location = buffers[1].searchLine(line);
        if (location != null) locations[locationIndex] = location;
        return locations;
    }

    public final LineBuffer getLine(long line) {
        Location[] locations = getLineLocations(line);
        if (locations[0] == null) return null;
        int last = locations[1] != null ? 1 : 0;
        int[] sizes = {0, 0};
        Location next = locations[last].buffer.searchNextLine(locations[last]);
        if (next == null) return null;
        int sizeIndex = 0;
        if (last > 0) {
            sizes[sizeIndex++] = locations[0].buffer.size - locations[0].bufferPos;
        }
        sizes[sizeIndex] = next.bufferPos - locations[last].bufferPos;

        byte[] buf = new byte[sizes[0] + sizes[1]];
        int index = 0;
        System.arraycopy(locations[0].buffer.byteBuffer, locations[0].bufferPos, buf, index, sizes[0]);
        if (last > 0) {
            index += sizes[0];
            System.arraycopy(locations[1].buffer.byteBuffer, locations[1].bufferPos, buf, index, sizes[1]);
        }
        return new LineBuffer(locations[0].position, locations[0].line, locations[0].col, buf);
    }

    private void pushPrefix(LineBuffer buffer, StringBuilder builder) {
        if (buffer.getCol() > 0) builder.append(STR_PREFIX);
    }

    private void pushLine(LineBuffer buffer, StringBuilder builder, String encoding) {
        if (buffer == null) return;
        builder.append(MessageFormat.format("(line: {0}, col: {1})\n", buffer.line, buffer.col));
        pushPrefix(buffer, builder);
        builder.append(buffer.getString(encoding));
    }

    public String getInfoNearLocation(long line, long col, int delta, boolean byBytes, String encoding) {
        int count = (delta << 1) + 1;
        LineBuffer[] lines = new LineBuffer[count];

        for (int i = 0; i < count; ++i) {
            lines[i] = getLine(line - delta + i);
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < count; ++i) {
            if (i != delta) {
                pushLine(lines[i], builder, encoding);
            } else {
                if (lines[i] == null) {
                    builder.append("WARNING: Can't display string near the position (grow up buffer size (application.properties -> filteredTextInputStream.bufferSize)");
                } else {
                    pushLine(lines[i], builder, encoding);
                    builder.append("\n");
                    pushPrefix(lines[i], builder);
                    if (byBytes) {
                        builder.append(lines[i].getPositionString(col, encoding));
                    } else {
                        builder.append(lines[i].getPositionString(col));
                    }
                    // hex push around
                }
            }
        }
        return builder.toString();
    }

    @Override
    public void close() throws IOException {
        if (stream != null) stream.close();
    }

    private int rest() {
        return getCurrentBuffer().size - currentBufferPosition;
    }

    private int doRead(byte[] buf, int off, int len) {
        int res = 0;
        while (len > 0) {
            int rest = this.rest();
            int count = Math.min(rest, len);
            if (count > 0) {
                System.arraycopy(getCurrentBuffer().byteBuffer, currentBufferPosition, buf, off, count);
                res += count;
                off += count;
                len -= count;
                currentBufferPosition += count;
                if (len > 0 && !swapBuffers()) {
                    break;
                }
            }
        }
        return res;
    }

    private int doRead() {
        byte[] buf = {0};
        return doRead(buf, 0, 1) == 0 ? -1 : buf[0];
    }

    @Override
    public int read(byte @NotNull [] b, int off, int len) {
        return doRead(b, off, len);
    }

    @Override
    public int read() {
        return doRead();
    }
}
