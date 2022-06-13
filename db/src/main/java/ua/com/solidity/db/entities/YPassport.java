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
	@ManyToOne(cascade = CascadeType.MERGE)
	@JsonBackReference
	@JoinColumn(name = "person_id")
	private YPerson person;
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "ypassport_import_source",
			joinColumns = {@JoinColumn(name = "ypassport_id")},
			inverseJoinColumns = {@JoinColumn(name = "import_source_id")}
	)
	private Set<ImportSource> importSources =  new HashSet<>();

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		YPassport yPassport = (YPassport) o;
		return Objects.equals(series, yPassport.series) && Objects.equals(number, yPassport.number) && Objects.equals(authority, yPassport.authority) && Objects.equals(issued, yPassport.issued) && Objects.equals(endDate, yPassport.endDate) && Objects.equals(recordNumber, yPassport.recordNumber) && Objects.equals(type, yPassport.type) && Objects.equals(validity, yPassport.validity) && Objects.equals(person, yPassport.person);
	}

	@Override
	public int hashCode() {
		return Objects.hash(series, number, authority, issued, endDate, recordNumber, type, validity, person);
	}

	@Override
	public Long getIdentifier() {
		return id;
	}
}
