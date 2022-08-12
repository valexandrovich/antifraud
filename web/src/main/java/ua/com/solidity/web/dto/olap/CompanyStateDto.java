package ua.com.solidity.web.dto.olap;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class CompanyStateDto implements Comparable<CompanyStateDto> {
    private String state;

    @Override
    public int compareTo(@NotNull CompanyStateDto o) {
        return this.state.compareTo(o.state);
    }
}
