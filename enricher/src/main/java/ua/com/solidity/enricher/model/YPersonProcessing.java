package ua.com.solidity.enricher.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
public class YPersonProcessing {
    private UUID uuid;
    private Long inn;
    private Integer passHash;
    private Integer personHash;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YPersonProcessing that = (YPersonProcessing) o;
        return Objects.equals(inn, that.inn) && Objects.equals(passHash, that.passHash) && Objects.equals(personHash, that.personHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inn, passHash, personHash);
    }
}