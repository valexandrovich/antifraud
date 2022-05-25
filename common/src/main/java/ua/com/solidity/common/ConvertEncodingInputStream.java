package ua.com.solidity.common;

import lombok.CustomLog;
import lombok.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;


@CustomLog
public class ConvertEncodingInputStream extends InputStream {
    private static final int BYTE_BUF_SIZE = 8192;
    private static final int CHAR_BUF_SIZE = 4096;
    private final InputStream stream;
    private CharsetEncoder encoder;
    CharBuffer charBuffer;
    ByteBuffer byteBuffer;
    private InputStreamReader reader;
    private boolean inputEOF = false;
    private boolean outputEOF = false;

    public ConvertEncodingInputStream(InputStream stream, Charset baseCharset, Charset targetCharset) {
        this.stream = stream;
        if (baseCharset != targetCharset) {
            byte[] buffer = new byte[BYTE_BUF_SIZE];
            byteBuffer = ByteBuffer.wrap(buffer);
            byte[] characterBuffer = new byte[CHAR_BUF_SIZE << 1];
            charBuffer = ByteBuffer.wrap(characterBuffer).asCharBuffer();
            encoder = targetCharset.newEncoder();
            reader = new InputStreamReader(stream, baseCharset);
            characterBufferNeeded();
        }
    }

    private void characterBufferNeeded() {
        charBuffer.clear();
        while (!inputEOF && charBuffer.hasRemaining()) {
            try {
                int count = reader.read(charBuffer);
                if (count < 0) {
                    inputEOF = true;
                    break;
                }
            } catch (Exception e) {
                log.error("ConvertEncodingInputStream Error.", e);
                inputEOF = true;
            }
        }
        charBuffer.flip();
        int mark = byteBuffer.position();
        byteBuffer.limit(byteBuffer.capacity());
        encoder.encode(charBuffer, byteBuffer, inputEOF);
        byteBuffer.flip();
        byteBuffer.position(mark);
    }

    private boolean byteBufferNeeded() {
        byteBuffer.clear();
        if (charBuffer.hasRemaining()) {
            byteBuffer.mark();
            encoder.encode(charBuffer, byteBuffer, inputEOF);
            byteBuffer.flip();
            byteBuffer.limit(byteBuffer.position());
            byteBuffer.reset();
        } else if (!inputEOF) {
            characterBufferNeeded();
        } else return false;
        return true;
    }

    private int doRead(byte[] buf, int off, int len) {
        int res = 0;
        while (len > 0 && !outputEOF) {
            int rest = byteBuffer.remaining();
            if (rest == 0 && byteBufferNeeded()) {
                rest = byteBuffer.remaining();
            }
            int count = Math.min(rest, len);
            if (count > 0) {
                byteBuffer.get(buf, off, count);
                len-= count;
                off+= count;
                res += count;
            } else if (count == 0 && inputEOF) {
                outputEOF = true;
            }
        }
        if (res == 0 && outputEOF) return -1;
        return res;
    }

    private int doRead() {
        byte[] buf = {0};
        return doRead(buf, 0, 1) == 0 ? -1 : buf[0];
    }

    @Override
    public int read(byte @NonNull [] b, int off, int len) throws IOException {
        if (reader == null) return stream.read(b, off, len);
        return doRead(b, off, len);
    }

    @Override
    public int read() throws IOException {
        if (reader == null) return stream.read();
        return doRead();
    }

    @Override
    public void close() {
        try {
            if (reader != null) reader.close();
            if (stream != null) stream.close();
        } catch (Exception e) {
            log.error("Error on close ConvertEncodingInputStream.");
        }
    }
}
