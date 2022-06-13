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
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "ytag")
public class YTag implements Identifiable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private LocalDate asOf;
	private LocalDate until;
	private String source;
	@ManyToOne(cascade = CascadeType.MERGE)
	@JsonBackReference
	@JoinColumn(name = "person_id")
	private YPerson person;
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "ytag_import_source",
			joinColumns = {@JoinColumn(name = "ytag_id")},
			inverseJoinColumns = {@JoinColumn(name = "import_source_id")}
	)
	private Set<ImportSource> importSources =  new HashSet<>();

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		YTag yTag = (YTag) o;
		return Objects.equals(name, yTag.name) && Objects.equals(asOf, yTag.asOf) && Objects.equals(until, yTag.until) && Objects.equals(source, yTag.source) && Objects.equals(person, yTag.person);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, asOf, until, source, person);
	}

	@Override
	public Long getIdentifier() {
		return id;
	}
}
