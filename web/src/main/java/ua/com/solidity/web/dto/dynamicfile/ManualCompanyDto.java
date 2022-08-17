package ua.com.solidity.web.dto.dynamicfile;

import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
@NoArgsConstructor
public class ManualCompanyDto implements Comparable<ManualCompanyDto> {
    private Long id;
    private UUID uuid;
    private String cnum;
    private String name;
    private String nameEn;
    private String shortName;
    private String edrpou;
    private String pdv;
    private String address;
    private String state;
    private String lname;
    private String fname;
    private String pname;
    private String inn;
    private String typeRelationPerson;
    private String cname;
    private String edrpouRelationCompany;
    private String typeRelationCompany;
    public Set<ManualTagDto> tags;

    @Override
    public int compareTo(@NotNull ManualCompanyDto o) {
        return (int) (this.getId() - o.getId());
    }
}
