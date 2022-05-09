package ua.com.solidity.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.com.solidity.web.entry.Person;

@NoArgsConstructor
@Getter
@Setter
public class PersonDto {

    public PersonDto(Person person) {
        this.id = person.getId().toString();
        this.username = person.getUsername();
        this.givenName = person.getGivenName();
        this.surname = person.getSurname();
        this.fullname = person.getFullName();
        this.displayname = person.getDisplayName();
    }

    private String id;

    private String username;

    private String givenName;

    private String surname;
    private String fullname;
    private String displayname;
}
