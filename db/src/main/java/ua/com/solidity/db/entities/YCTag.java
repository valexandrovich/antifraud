package ua.com.solidity.db.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import ua.com.solidity.db.abstraction.Identifiable;

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
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "yctag")
public class YCTag implements Identifiable {
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
	@JoinColumn(name = "company_id")
	private YCompany company;
	@ManyToMany
	@JoinTable(
			name = "yctag_import_source",
			joinColumns = {@JoinColumn(name = "yctag_id")},
			inverseJoinColumns = {@JoinColumn(name = "import_source_id")}
	)
	private Set<ImportSource> importSources =  new HashSet<>();

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		YCTag ycTag = (YCTag) o;
		return Objects.equals(tagType, ycTag.tagType) && Objects.equals(asOf, ycTag.asOf) && Objects.equals(until, ycTag.until) && Objects.equals(source, ycTag.source) && Objects.equals(company, ycTag.company);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tagType, asOf, until, source, company);
	}

	@Override
	@JsonIgnore
	public Long getIdentifier() {
		return id;
	}
}
