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
        base = "ou=Flex Cube Implementation Department,ou=Software Development and IT Architecture,ou=ITS",
        objectClasses = {"posixAccount", "inetOrgPerson", "top"})
public class Person {
    @Id
    @ToString.Exclude
    private Name id;

    @Attribute(name = "uid")
    private String username;
    @Attribute(name = "givenName")
    private String givenName;
    @Attribute(name = "sn")
    private String surname;
    @Attribute(name = "cn")
    private String fullname;
    @Attribute(name = "displayName")
    private String displayname;
    @Attribute(name = "userPassword")
    private String password;
}
