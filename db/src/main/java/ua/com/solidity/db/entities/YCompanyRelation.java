package ua.com.solidity.db.entities;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ycompany_relation")
@NoArgsConstructor
public class YCompanyRelation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "company_id", referencedColumnName = "id")
	private YCompany company;

	@ManyToOne
	@JoinColumn(name = "role_id", referencedColumnName = "id")
	private YCompanyRole role;

	@ManyToOne
	@JsonBackReference
	@JoinColumn(name = "person_id", referencedColumnName = "id")
	private YPerson person;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof YCompanyRelation)) return false;
        YCompanyRelation that = (YCompanyRelation) o;
        return Objects.equals(company, that.company) && Objects.equals(role, that.role) && Objects.equals(person, that.person);
    }

    @Override
    public int hashCode() {
        return Objects.hash(company, role, person);
    }
}
