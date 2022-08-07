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
public class ManualPersonDto implements Comparable<ManualPersonDto> {
    private Long id;
    private UUID uuid;
    private String cnum;
    private String lnameUk;
    private String fnameUk;
    private String pnameUk;
    private String lnameRu;
    private String fnameRu;
    private String pnameRu;
    private String lnameEn;
    private String fnameEn;
    private String pnameEn;
    private String birthday;
    private String okpo;
    private String country;
    private String address;
    private String phone;
    private String email;
    private String birthPlace;
    private String sex;
    private String comment;
    private String passLocalNum;
    private String passLocalSerial;
    private String passLocalIssuer;
    private String passLocalIssueDate;
    private String passIntNum;
    private String passIntRecNum;
    private String passIntIssuer;
    private String passIntIssueDate;
    private String passIdNum;
    private String passIdRecNum;
    private String passIdIssuer;
    private String passIdIssueDate;
    private Set<ManualTagDto> tags;

    @Override
    public int compareTo(@NotNull ManualPersonDto o) {
        return (int) (this.getId() - o.getId());
    }
}
