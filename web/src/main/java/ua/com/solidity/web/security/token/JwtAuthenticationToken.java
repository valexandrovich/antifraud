package ua.com.solidity.web.security.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private String rawAccessToken;
    private UserDetails userDetails;

    public JwtAuthenticationToken(String rawAccessToken) {
        super(null);
        this.rawAccessToken = rawAccessToken;
        super.setAuthenticated(false);
    }

    public JwtAuthenticationToken(UserDetails userDetails, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.userDetails = userDetails;
        super.setAuthenticated(true);
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        if (authenticated) {
            throw new IllegalArgumentException(
                    "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        }
        super.setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return rawAccessToken;
    }

    @Override
    public Object getPrincipal() {
        return this.userDetails;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.rawAccessToken = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        JwtAuthenticationToken that = (JwtAuthenticationToken) o;

        if (!Objects.equals(rawAccessToken, that.rawAccessToken))
            return false;
        return Objects.equals(userDetails, that.userDetails);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (rawAccessToken != null ? rawAccessToken.hashCode() : 0);
        result = 31 * result + (userDetails != null ? userDetails.hashCode() : 0);
        return result;
    }
}
