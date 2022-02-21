package ua.com.solidity.web;

import org.junit.Assert;
import org.junit.Test;

import java.text.MessageFormat;

public class FormatTest {

    @Test
    public void bracesShouldBeSubstitutedTest() {
        Assert.assertEquals("(uid=name)", MessageFormat.format("(uid={0})", "name"));
    }
}
