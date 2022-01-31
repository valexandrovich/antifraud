package ua.com.solidity.ad.entry;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entry(
        objectClasses = {"user", "top", "organizationalPerson"})
public final class Person {
    @Id
    @ToString.Exclude
    private Name id;

    @Attribute(name = "givenName")
    private String givenName;
    @Attribute(name = "sn")
    private String surname;
    @Attribute(name = "cn")
    private String fullname;
    @Attribute(name = "displayName")
    private String displayname;
    @Attribute(name = "sAMAccountName")
    private String username;
}
