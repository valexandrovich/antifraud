package ua.com.solidity.common.parsers.json;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.com.solidity.common.CustomTreeObjectParams;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JSONParams extends CustomTreeObjectParams {
    private String encoding = "UTF-8";

    public final Charset getCharset() {
        return Charset.availableCharsets().getOrDefault(encoding, StandardCharsets.UTF_8);
    }
}
