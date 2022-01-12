package ua.com.solidity.otp.web.security.servise;

public interface JwtExtractor {

    String extract(String payload);

}
