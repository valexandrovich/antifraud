package ua.com.solidity.otp.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.com.solidity.ad.entry.Person;

@NoArgsConstructor
@Getter
@Setter
public class PersonDto {

    public PersonDto(Person person) {
        this.id = person.getId().toString();
        this.username = person.getUsername();
        this.givenName = person.getGivenName();
        this.surname = person.getSurname();
        this.fullname = person.getFullname();
        this.displayname = person.getDisplayname();
        this.password = person.getPassword();
    }

    private String id;

    private String username;

    private String givenName;

    private String surname;
    private String fullname;
    private String displayname;
    private String password;
}
