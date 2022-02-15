package ua.com.solidity.common;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UrlTest {
    private static final String testUrl = "jdbc:PostgreSql://user:password@example.com.ua:1000/xxx?hello&world=2#topic";
    private static final String testRes = "jdbc:PostgreSql://user:password@example.com.ua:1000/xxx?hello&world=true&extra=true#topic";
    @Test
    void doFirstUrlTest() {
        DBUtils.mergeURLParams(testUrl, "extra=true&world=true");
        assertThat(DBUtils.mergeURLParams(testUrl, "world=true&extra=true")).isEqualTo(testRes);
    }
}
