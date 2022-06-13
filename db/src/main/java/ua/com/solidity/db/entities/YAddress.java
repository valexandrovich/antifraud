package ua.com.solidity.db.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import ua.com.solidity.db.abstraction.Identifiable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "yaddress")
public class YAddress implements Identifiable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String address;
	@ManyToOne(cascade = CascadeType.MERGE)
	@JsonBackReference
	@JoinColumn(name = "person_id")
	private YPerson person;
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "yaddress_import_source",
			joinColumns = {@JoinColumn(name = "yaddress_id")},
			inverseJoinColumns = {@JoinColumn(name = "import_source_id")}
	)
	private Set<ImportSource> importSources =  new HashSet<>();

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		YAddress yAddress = (YAddress) o;
		return Objects.equals(address, yAddress.address) && Objects.equals(person, yAddress.person);
	}

	@Override
	public int hashCode() {
		return Objects.hash(address, person);
	}

	@Override
	public Long getIdentifier() {
		return id;
	}
}
