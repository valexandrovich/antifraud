package ua.com.solidity.web.security.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ua.com.solidity.web.entry.Person;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LoginUserDetails implements UserDetails {

    private final String username;
    private final boolean active;
    private final List<GrantedAuthority> authorities;

    public LoginUserDetails(Person person, String role) {
        this.username = person.getDisplayname();
        this.active = true;
        if (role == null) { authorities = new ArrayList<>();
        } else authorities = Stream.of(role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public String getSimpleRole() {
        if (!authorities.isEmpty()) {
            return authorities.get(0).toString();
        } else return null;
    }

    public List<String> getRoles() {
        return this.authorities.stream().map((s) -> toString()).collect(Collectors.toList());
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
