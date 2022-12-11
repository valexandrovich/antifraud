import org.junit.Assert;
import org.junit.Test;

import static ua.com.solidity.report.utils.Utils.*;

public class UtilsTest {
    @Test
    public void shouldBeCorrectHexTest() {
        byte[] values = {0, 1, 2, 3, 4, 5, 6};

        String s = bytesToHex(values);

        Assert.assertEquals("00010203040506", s);
    }

    @Test
    public void shouldBeCorrectPathTest() {
        byte[] values = {0, 1, 2, 3, 4, 5, 6};

        String s = pathFromName(values);

        Assert.assertEquals("00/01/02/", s);
    }

    @Test
    public void shouldHaveCorrectSizeTest() {
        String[] s = randomName();

        Assert.assertEquals(9, s[0].length());
        Assert.assertEquals(64, s[1].length());
    }
}
