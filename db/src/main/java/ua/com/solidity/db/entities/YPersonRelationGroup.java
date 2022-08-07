package ua.com.solidity.db.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "yperson_relation_group")
@NoArgsConstructor
@AllArgsConstructor
public class YPersonRelationGroup {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "relation_type_id", referencedColumnName = "id", nullable = false)
	private YPersonRelationType relationType;

	public YPersonRelationGroup(YPersonRelationType relationType) {
		this.relationType = relationType;
	}
}
