package com.reaksmey.blog.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

	private final SecretKey signingKey;
	private final Long EXPIRATION_TIME;

	public JwtService(
		@Value("${application.security.jwt.secret-key}") String secretKey,
		@Value("${application.security.jwt.expiration}") Long expirationTime
	) {

		this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
		this.EXPIRATION_TIME = expirationTime;
	}

	public String generateToken(String username) {

		return Jwts.builder()
			.claims()
			.subject(username)
			.issuedAt(new Date(System.currentTimeMillis()))
			.expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
			.and()
			.signWith(signingKey)
			.compact();
	}

	private Claims extractAllClaims(String token) {

		return Jwts.parser()
			.verifyWith(signingKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {

		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	public boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	public boolean isValidToken(String token, UserDetails userDetails) {

		final String username = extractUsername(token);
		boolean isUsernameValid = username.equals(userDetails.getUsername());
		boolean isTokenExpired = isTokenExpired(token);

		return isUsernameValid && !isTokenExpired;
	}
}
