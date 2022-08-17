package ua.com.solidity.db.entities;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "tag_type")
@Entity
public class TagType {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true)
	private Long id;
	@Column(name = "code", unique = true, nullable = false)
	private String code;
	@Column(name = "description", unique = true, nullable = false)
	private String description;
	@Column(name = "physical", nullable = false)
	private boolean physical;
	@Column(name = "juridical", nullable = false)
	private boolean juridical;

	public TagType(String code, String description) {
		this.code = code;
		this.description = description;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
		TagType tagType = (TagType) o;
		return id != null && Objects.equals(id, tagType.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
