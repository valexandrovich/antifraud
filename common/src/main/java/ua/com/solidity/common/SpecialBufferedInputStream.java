package ua.com.solidity.common;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

public class SpecialBufferedInputStream extends BufferedInputStream implements SpecialInputStream {
    public SpecialBufferedInputStream(InputStream in, int size) {
        super(Utils.getSpecialInputStream(in), size);
    }

    @Override
    public Charset getCharset() {
        if (in instanceof SpecialInputStream) {
            return ((SpecialInputStream) in).getCharset();
        } else {
            return Utils.getInputStreamCharset(in);
        }
    }
}
