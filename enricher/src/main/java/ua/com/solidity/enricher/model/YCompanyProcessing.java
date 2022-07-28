package ua.com.solidity.enricher.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
public class YCompanyProcessing {
    private UUID uuid;
    private Long edrpou;
    private Long pdv;
    private Integer companyHash;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YCompanyProcessing that = (YCompanyProcessing) o;
        return Objects.equals(edrpou, that.edrpou) && Objects.equals(pdv, that.pdv) && Objects.equals(companyHash, that.companyHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(edrpou, pdv, companyHash);
    }
}
