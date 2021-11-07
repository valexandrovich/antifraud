package ua.com.solidity.common;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public abstract class CustomParser {
    protected final Map<String, String> fields = new HashMap<>();
    public abstract boolean eof();
    public abstract boolean next();
}
