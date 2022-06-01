package ua.com.solidity.web.security.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ua.com.solidity.web.security.exception.JwtTokenExpiredException;
import ua.com.solidity.web.security.model.UserDetailsImpl;
import ua.com.solidity.web.security.token.JwtToken;

import java.util.Date;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtUtilService {

	@Value("#{new Integer('${token.duration.minutes}')}")
	private Integer tokenDuration;
	@Value("${token.secret.key}")
	private String secretKey;

	private static final String NUMBER_REGEX = "^[0-9]+$";

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
			log.error("Invalid JWT Token: {}", ex.getMessage());
			throw new BadCredentialsException("Invalid JWT token: ", ex);
		} catch (ExpiredJwtException expiredEx) {
			log.info("JWT Token is expired: {}", expiredEx.getMessage());
			throw new JwtTokenExpiredException(token, "JWT Token expired", expiredEx);
		}
	}

	private Boolean isTokenExpired(JwtToken token) {
		return extractExpiration(token).before(new Date());
	}

	public JwtToken generateToken(UserDetailsImpl userDetails) {
		Claims claims = Jwts.claims().setSubject(userDetails.getUsername());
		claims.put("role", userDetails.getSimpleRole());
		claims.put("user", userDetails.getDisplayName());
		String token = createToken(claims);
		return new JwtToken(token);
	}

	private String createToken(Claims claims) {
		long timeMillis = System.currentTimeMillis();
		return Jwts.builder()
				.setClaims(claims)
				.setIssuedAt(new Date(timeMillis))
				.setExpiration(new Date(timeMillis + 1000L * 60 * tokenDuration))
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
