package ua.com.solidity.db.entities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "yperson_relation")
@NoArgsConstructor
@AllArgsConstructor
public class YPersonRelation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JsonBackReference
	@JoinColumn(name = "person_id", nullable = false)
	private YPerson person;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "relation_group_id", referencedColumnName = "id", nullable = false)
	private YPersonRelationGroup relationGroup;
}
