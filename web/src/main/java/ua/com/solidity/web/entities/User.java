package ua.com.solidity.web.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.com.solidity.db.entities.Role;
import ua.com.solidity.db.entities.YPerson;
import ua.com.solidity.web.entry.Person;

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
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

	public User(Person person, Role role) {
		this.givenName = person.getGivenName();
		this.surname = person.getSurname();
		this.fullName = person.getFullName();
		this.displayName = person.getDisplayName();
		this.username = person.getUsername();
		this.email = person.getEmail();
		this.phoneNumber = person.getPhoneNumber();
		this.role = role;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "given_name")
	private String givenName;

	@Column(name = "surname")
	private String surname;

	@Column(name = "full_name")
	private String fullName;

	@Column(name = "display_name")
	private String displayName;

	@Column(name = "username", unique = true)
	private String username;

	@Column(name = "email")
	private String email;

	@Column(name = "phone_number")
	private String phoneNumber;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "role_id", referencedColumnName = "id")
	private Role role;

	@ManyToMany
	@JoinTable(
			name = "users_yperson",
			joinColumns = {@JoinColumn(name = "users_id")},
			inverseJoinColumns = {@JoinColumn(name = "yperson_id")}
	)
	private Set<YPerson> people = new HashSet<>();
}
