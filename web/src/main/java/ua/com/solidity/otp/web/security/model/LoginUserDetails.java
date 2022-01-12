package ua.com.solidity.otp.web.security.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ua.com.solidity.ad.entry.Person;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LoginUserDetails implements UserDetails {

    private final String username;
    private final String password;
    private final boolean active;
    private final List<GrantedAuthority> authorities;

    public LoginUserDetails(Person person, Role role) {
        this.username = person.getDisplayname();
        this.password = person.getPassword();
        this.active = true;
        this.authorities = Stream.of(role.getRole())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public String getSimpleRole() {
        return authorities.get(0).toString();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
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
