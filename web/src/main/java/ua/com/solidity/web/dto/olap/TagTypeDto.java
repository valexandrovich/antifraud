package ua.com.solidity.web.dto.olap;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class TagTypeDto implements Comparable<TagTypeDto>{
    private String code;

    @Override
    public int compareTo(@NotNull TagTypeDto o) {
        return this.code.compareTo(o.code);
    }
}
