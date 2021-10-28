package ua.com.solidity.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImporterMessageData {
    private String format;
    private long size;
    private String dataFileName;
    private String infoFileName;
}
