package ua.com.solidity.otp.web.security.servise;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ua.com.solidity.otp.web.security.exception.JwtTokenExpiredException;
import ua.com.solidity.otp.web.security.token.JwtToken;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtUtilService {

    private final String secretKey = "fwijn23845lkasd--&%#E";

    public String extractUserLogin(JwtToken token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(JwtToken token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(JwtToken token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(JwtToken token) {
        log.debug("Parsing claims");
        try {
            return Jwts.parser().setSigningKey(getSecretKey()).parseClaimsJws(token.getToken()).getBody();
        } catch (UnsupportedJwtException | MalformedJwtException | IllegalArgumentException | SignatureException ex) {
            log.error("Invalid JWT Token", ex);
            throw new BadCredentialsException("Invalid JWT token: ", ex);
        } catch (ExpiredJwtException expiredEx) {
            log.info("JWT Token is expired", expiredEx);
            throw new JwtTokenExpiredException(token, "JWT Token expired", expiredEx);
        }

    }

    private Boolean isTokenExpired(JwtToken token) {
        return extractExpiration(token).before(new Date());
    }

    public JwtToken generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        String token = createToken(claims, userDetails.getUsername());
        return new JwtToken(token);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        long timeMillis = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(timeMillis))
                .setExpiration(new Date(timeMillis + 1000 * 60 * 60 * 10))
                .signWith(SignatureAlgorithm.HS256, getSecretKey())
                .compact();
    }

    public Boolean validateToken(JwtToken token, UserDetails userDetails) {
        final String userName = extractUserLogin(token);
        return (userName.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private String getSecretKey() {
        return secretKey;
    }

}
