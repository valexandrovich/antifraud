package ua.com.solidity.web.dto.olap;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class CompanyRoleDto implements Comparable<CompanyRoleDto> {
    private String role;

    @Override
    public int compareTo(@NotNull CompanyRoleDto o) {
        return this.role.compareTo(o.role);
    }
}
