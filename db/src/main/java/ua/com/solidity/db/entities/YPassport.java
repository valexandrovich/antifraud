package ua.com.solidity.db.entities;

import java.time.LocalDate;
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
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import ua.com.solidity.db.abstraction.Identifiable;

@Getter
@Setter
@Entity
@NamedEntityGraph(name="ypassport.sources", attributeNodes= @NamedAttributeNode("importSources"))
@Table(name = "ypassport")
public class YPassport implements Identifiable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String series;
	private Integer number;
	private String authority;
	private LocalDate issued;
	private LocalDate endDate;
	private String recordNumber;
	private String type;
	private Boolean validity; // true - for valid passport
	@ManyToMany(mappedBy = "passports")
	@JsonBackReference
	private Set<YPerson> people = new HashSet<>();
	@ManyToMany
	@JoinTable(
			name = "ypassport_import_source",
			joinColumns = {@JoinColumn(name = "ypassport_id")},
			inverseJoinColumns = {@JoinColumn(name = "import_source_id")}
	)
	private Set<ImportSource> importSources =  new HashSet<>();

	public void cleanAssociations() {
		this.people.forEach(yPerson -> {
			yPerson.getPassports().removeIf(passport -> id.equals(passport.getId()));
		});
		this.importSources = new HashSet<>();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		YPassport yPassport = (YPassport) o;
		return Objects.equals(series, yPassport.series) && Objects.equals(number, yPassport.number) && Objects.equals(type, yPassport.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(series, number, type);
	}

	@Override
	public Long getIdentifier() {
		return id;
	}
}
