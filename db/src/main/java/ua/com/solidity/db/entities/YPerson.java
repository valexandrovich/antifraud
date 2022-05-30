package ua.com.solidity.db.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class YPerson {

	public YPerson(UUID id) {
		this.id = id;
	}

	@Id
	private UUID id;
	private String lastName;
	private String firstName;
	private String patName;
	private LocalDate birthdate;
	@OneToMany(cascade = CascadeType.MERGE, mappedBy = "person", fetch = FetchType.EAGER)
	@JsonManagedReference
	private Set<YINN> inns = new HashSet<>();
	@OneToMany(cascade = CascadeType.MERGE, mappedBy = "person", fetch = FetchType.EAGER)
	@JsonManagedReference
	private Set<YAddress> addresses = new HashSet<>();
	@OneToMany(cascade = CascadeType.MERGE, mappedBy = "person", fetch = FetchType.EAGER)
	@JsonManagedReference
	private Set<YAltPerson> altPeople = new HashSet<>();
	@OneToMany(cascade = CascadeType.MERGE, mappedBy = "person", fetch = FetchType.EAGER)
	@JsonManagedReference
	private Set<YPassport> passports = new HashSet<>();
	@OneToMany(cascade = CascadeType.MERGE, mappedBy = "person", fetch = FetchType.EAGER)
	@JsonManagedReference
	private Set<YTag> tags = new HashSet<>();
	@OneToMany(cascade = CascadeType.MERGE, mappedBy = "person", fetch = FetchType.EAGER)
	@JsonManagedReference
	private Set<YEmail> emails = new HashSet<>();
	@OneToMany(cascade = CascadeType.MERGE, mappedBy = "person", fetch = FetchType.EAGER)
	@JsonManagedReference
	private Set<YPhone> phones = new HashSet<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "users_yperson",
			joinColumns = {@JoinColumn(name = "yperson_id")},
			inverseJoinColumns = {@JoinColumn(name = "users_id")}
	)
	private Set<User> users = new HashSet<>();
}
