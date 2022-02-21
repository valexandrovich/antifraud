package ua.com.solidity.web.security.service;

public interface JwtExtractor {

    String extract(String payload);

}
