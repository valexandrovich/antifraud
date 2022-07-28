package ua.com.solidity.db.entities;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
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
import lombok.Getter;
import lombok.Setter;
import ua.com.solidity.db.abstraction.Identifiable;

@Getter
@Setter
@Entity
@Table(name = "ytag")
public class YTag implements Identifiable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
    @ManyToOne
    @JoinColumn(name = "tag_type_id")
    private TagType tagType;
	@Column(name = "as_Of")
	private LocalDate asOf;
	@Column(name = "until")
	private LocalDate until;
	@Column(name = "source", length = 1100)
	private String source;
	@ManyToOne
	@JsonBackReference
	@JoinColumn(name = "person_id")
	private YPerson person;

	@ManyToMany
	@JoinTable(
			name = "ytag_import_source",
			joinColumns = {@JoinColumn(name = "ytag_id")},
			inverseJoinColumns = {@JoinColumn(name = "import_source_id")}
	)
	private Set<ImportSource> importSources =  new HashSet<>();

	public void cleanAssociations() {
		this.person.getTags().removeIf(tag -> id.equals(tag.getId()));
		this.importSources = new HashSet<>();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		YTag yTag = (YTag) o;
		return Objects.equals(tagType, yTag.tagType) && Objects.equals(asOf, yTag.asOf) && Objects.equals(until, yTag.until) && Objects.equals(source, yTag.source) && Objects.equals(person, yTag.person);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tagType, asOf, until, source, person);
	}

	@Override
	public Long getIdentifier() {
		return id;
	}
}
