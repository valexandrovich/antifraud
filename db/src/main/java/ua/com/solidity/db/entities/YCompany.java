package ua.com.solidity.db.entities;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NamedEntityGraphs({
		@NamedEntityGraph(
				name = "ycompany.tagsAndSources",
				attributeNodes = {
						@NamedAttributeNode("tags"),
						@NamedAttributeNode("importSources")
				})
})
@Entity
@Table(name = "ycompany")
public class YCompany {

	@Id
	@Column(name = "id")
	private UUID id;

	@Column(name = "edrpou")
	private Long edrpou;

	@Column(name = "name")
	private String name;

	@OneToMany(cascade = CascadeType.MERGE, mappedBy = "company")
	@JsonManagedReference
	private Set<YCTag> tags = new HashSet<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "ycompany_import_source",
			joinColumns = {@JoinColumn(name = "ycompany_id")},
			inverseJoinColumns = {@JoinColumn(name = "import_source_id")}
	)
	private Set<ImportSource> importSources = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YCompany yCompany = (YCompany) o;
        return Objects.equals(edrpou, yCompany.edrpou) && Objects.equals(name, yCompany.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(edrpou, name);
    }
}
