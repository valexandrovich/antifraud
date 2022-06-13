package ua.com.solidity.db.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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

	public TagType(String code, String description) {
		this.code = code;
		this.description = description;
	}
}
