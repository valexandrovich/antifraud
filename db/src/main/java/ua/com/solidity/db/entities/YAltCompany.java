package ua.com.solidity.db.entities;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
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
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import ua.com.solidity.db.abstraction.Identifiable;

@Getter
@Setter
@Entity
@Table(name = "yaltcompany")
public class YAltCompany implements Identifiable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "name", length = 550)
	private String name;

	@Column(name = "language")
	private String language;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "company_id")
    private YCompany company;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "yaltcompany_import_source",
			joinColumns = {@JoinColumn(name = "yaltcompany_id")},
			inverseJoinColumns = {@JoinColumn(name = "import_source_id")}
	)
	private Set<ImportSource> importSources = new HashSet<>();

	@Override
	public Long getIdentifier() {
		return id;
	}
}
