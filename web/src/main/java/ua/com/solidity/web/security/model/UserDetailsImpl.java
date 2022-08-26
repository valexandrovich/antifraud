package ua.com.solidity.web.security.model;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.naming.Name;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ua.com.solidity.web.entry.Person;

public class UserDetailsImpl implements UserDetails {

    private final String dn;
    private final String displayName;
    private final String username;
    private final boolean active;
    private final List<GrantedAuthority> authorities;

    public UserDetailsImpl(Person person, String role) {
        Name name = person.getId();
        this.dn = name != null ? name.toString() : null;
        this.displayName = person.getDisplayName();
        this.username = person.getUsername();
        this.active = true;
        if (role == null) { authorities = AuthorityUtils.NO_AUTHORITIES;
        } else authorities = Stream.of(role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public String getDn() {
        return dn;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSimpleRole() {
        if (!authorities.isEmpty()) {
            return authorities.get(0).toString();
        } else return null;
    }

    public List<String> getRoles() {
        return this.authorities.stream().map(s -> toString()).collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return active;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return active;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

}
