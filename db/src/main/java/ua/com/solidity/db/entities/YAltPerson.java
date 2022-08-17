package ua.com.solidity.db.entities;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import ua.com.solidity.db.abstraction.Identifiable;

@Getter
@Setter
@Entity
@Table(name = "yaltperson")
public class YAltPerson implements Identifiable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String lastName;
	private String firstName;
	private String patName;
	private String language;
	@ManyToOne
	@JsonBackReference
	@JoinColumn(name = "person_id")
	private YPerson person;
	@ManyToMany
	@JoinTable(
			name = "yaltperson_import_source",
			joinColumns = {@JoinColumn(name = "yaltperson_id")},
			inverseJoinColumns = {@JoinColumn(name = "import_source_id")}
	)
	private Set<ImportSource> importSources =  new HashSet<>();

	public void cleanAssociations() {
		this.person.getAltPeople().removeIf(altPerson -> id.equals(altPerson.getId()));
		this.importSources = new HashSet<>();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		YAltPerson that = (YAltPerson) o;
		return Objects.equals(lastName, that.lastName) && Objects.equals(firstName, that.firstName) && Objects.equals(patName, that.patName) && Objects.equals(language, that.language);
	}

	@Override
	public int hashCode() {
		return Objects.hash(lastName, firstName, patName, language);
	}

	@Override
	@JsonIgnore
	public Long getIdentifier() {
		return id;
	}
}
