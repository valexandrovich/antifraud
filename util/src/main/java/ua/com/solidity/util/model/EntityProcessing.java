package ua.com.solidity.util.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntityProcessing {
    private UUID uuid;
    private long inn = 0;
    private int passHash = 0;
    private int personHash = 0;
    private long edrpou = 0;
    private long pdv = 0;
    private int companyHash = 0;
    private LocalDateTime addingTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntityProcessing)) return false;
        EntityProcessing that = (EntityProcessing) o;
        return inn == that.inn && passHash == that.passHash && personHash == that.personHash && edrpou == that.edrpou && pdv == that.pdv && companyHash == that.companyHash;
    }

    @Override
    public int hashCode() {
        return Objects.hash(inn, passHash, personHash, edrpou, pdv, companyHash);
    }
}
