package ua.com.solidity.common;

import com.fasterxml.jackson.core.JsonLocation;
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
    private static final int ERROR_MARKER_SHIFT = 1;
    private static final String ERROR_MARKER = " ^ (ERROR) ";

    private enum CRLFState {
        NORMAL,
        CARRIAGE_RETURN
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
            buffer = copy.buffer;
        }

        public final void copyFrom(Location location) {
            line = location.line;
            col = location.col;
            position = location.position;
            state = location.state;
            bufferPos = location.bufferPos;
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
                    deferredNewLine = true;
                } else if (value == BYTE_CARRIAGE_RETURN) {
                    state = CRLFState.CARRIAGE_RETURN;
                }
            } else if ((state == CRLFState.CARRIAGE_RETURN && value == BYTE_LINE_FEED)) {
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

        public final void align() {
            while (col > 0 && bufferPos > 0) {
                --bufferPos;
                --position;
                --col;
            }
        }
    }

    @Getter
    @Setter
    private static class LineBuffer {
        private static final String ELLIPSIS = "...";
        public final long line;
        public final long position;
        public final long col;
        public final byte[] bytes;
        private int size;
        private boolean terminated;
        public LineBuffer(long position, long line, long col, byte[] bytes) {
            this.position = position;
            this.line = line;
            this.col = col;
            this.bytes = bytes;
            this.size = this.bytes.length;
            terminated = false;
            while (size > 0 && (bytes[size - 1] == BYTE_LINE_FEED || bytes[size - 1] == BYTE_CARRIAGE_RETURN)) {
                size--;
                terminated = true;
            }
        }

        public final String getPrefix() {
            return col > 0 ? ELLIPSIS : "";
        }

        public final String getSuffix() {
            return terminated ? "" : ELLIPSIS;
        }

        public final String getString(String encoding) {
            ByteArrayInputStream stream = new ByteArrayInputStream(bytes, 0, size);
            InputStreamReader reader = new InputStreamReader(stream, Charset.availableCharsets().getOrDefault(encoding, StandardCharsets.UTF_8));
            Scanner scanner = new Scanner(reader);
            return scanner.nextLine();
        }
    }

    @Getter
    @Setter
    private static class Buffer {
        private final byte[] byteBuffer;
        private int size = 0;
        boolean eof = false;
        Location start = new Location();
        Location finish = new Location();

        public Buffer(int bufferSize) {
            byteBuffer = new byte[bufferSize];
            start.setBuffer(this);
            finish.setBuffer(this);
        }

        public final void prepareNextBuffer(Buffer buf) {
            buf.size = 0;
            buf.start.copyFrom(finish);
            buf.start.setBufferPos(0);
            buf.finish.copyFrom(buf.start);
        }

        public final boolean loadBuffer(InputStream stream) {
            try {
                size = 0;
                while (size < byteBuffer.length) {
                    int count = stream.read(byteBuffer, size, byteBuffer.length - size);
                    if (count > 0) {
                        for (int i = 0; i < count; ++i) {
                            int value = byteBuffer[i + size] & 0xff;
                            if (value < 32 && value != 9 && value != 10 && value != 13) byteBuffer[i + size] = 32;
                            finish.pushByte(byteBuffer[i + size]);
                        }
                        size += count;
                    } else if (count < 0) {
                        eof = true;
                        break;
                    }
                }
                return size > 0;
            } catch (Exception e) {
                log.error("Read error", e);
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
            if (location.line != res.line) res.align();
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
        if (stream != null) buffers[1].loadBuffer(stream);
    }

    private boolean swapBuffers() {
        if (buffers[1].eof) return false;
        currentBufferPosition = 0;
        Buffer temp = buffers[0];
        buffers[0] = buffers[1];
        buffers[1] = temp;
        buffers[0].prepareNextBuffer(temp);
        hasPriorBuffer = true;
        return (buffers[1].loadBuffer(stream));
    }

    public final long getPosition() {
        return buffers[1].start.getPosition() + currentBufferPosition;
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
        if (next == null) {
            log.warn("Internal error: can't find a next line cursor position.");
            return null;
        }
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
        builder.append(buffer.getPrefix());
    }

    private void pushSuffix(LineBuffer buffer, StringBuilder builder) {
        builder.append(buffer.getSuffix());
        builder.append("\n");
    }

    private void pushLine(LineBuffer buffer, StringBuilder builder, String encoding) {
        if (buffer == null) return;
        builder.append(MessageFormat.format("(pos: {0}, line: {1}, col: {2})\n", buffer.position, buffer.line + 1, buffer.col + 1));
        pushPrefix(buffer, builder);
        builder.append(buffer.getString(encoding));
        pushSuffix(buffer, builder);
    }

    public ErrorReport getErrorReport(JsonLocation location, String encoding) {
        LineBuffer buffer = getLine(location.getLineNr() - 1L);
        if (buffer != null) {
            String prefix = buffer.getPrefix();
            String suffix = buffer.getSuffix();
            String text = prefix +
                    buffer.getString(encoding) +
                    suffix;
            long locationCol = location.getColumnNr();
            return new ErrorReport(buffer.line + 1, locationCol,
                    location.getByteOffset(), location.getCharOffset(),
                    locationCol - buffer.col + prefix.length() + 1, text, null);
        }

        return new ErrorReport(location.getLineNr(), location.getColumnNr(), location.getByteOffset(), location.getCharOffset(), -1,
                "<out of buffer>", null);
    }

    public String getInfoNearLocation(long line, long col, int delta, String encoding) {
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
                    builder.append("error line : ");
                    pushLine(lines[i], builder, encoding);
                    pushPrefix(lines[i], builder);
                    long spaceCount = col - lines[i].col - ERROR_MARKER_SHIFT;
                    builder.append(".".repeat((int) spaceCount)).append(ERROR_MARKER);
                    spaceCount = lines[i].size - spaceCount - ERROR_MARKER.length();
                    builder.append(".".repeat((int) spaceCount));
                    pushSuffix(lines[i], builder);
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
        return buffers[1].size - currentBufferPosition;
    }

    private int doRead(byte[] buf, int off, int len) {
        int res = 0;
        while (len > 0) {
            int rest = this.rest();
            int count = Math.min(rest, len);
            if (count > 0) {
                System.arraycopy(buffers[1].byteBuffer, currentBufferPosition, buf, off, count);
                res += count;
                off += count;
                len -= count;
                currentBufferPosition += count;
            }
            if (len > 0 && !swapBuffers()) {
                break;
            }
        }
        return res == 0 ? -1 : res;
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
