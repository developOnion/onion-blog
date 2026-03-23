package com.reaksmey.blog.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

	private final SecretKey signingKey;
	private final Long EXPIRATION_TIME;
	private final Long REFRESH_EXPIRATION_TIME;

	public JwtService(
		@Value("${application.security.jwt.secret-key}") String secretKey,
		@Value("${application.security.jwt.expiration}") Long expirationTime,
		@Value("${application.security.jwt.refresh-token.expiration}") Long refreshExpirationTime
	) {

		this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
		this.EXPIRATION_TIME = expirationTime;
		this.REFRESH_EXPIRATION_TIME = refreshExpirationTime;
	}

	public String generateToken(UserDetails userDetails) {
		return generateToken(new HashMap<>(), userDetails);
	}

	public String generateToken(
		Map<String, Object> extraClaims,
		UserDetails userDetails
	) {
		return buildToken(extraClaims, userDetails, EXPIRATION_TIME);
	}

	public String generateRefreshToken(UserDetails userDetails) {
		return buildToken(new HashMap<>(), userDetails, REFRESH_EXPIRATION_TIME);
	}

	private String buildToken(
		Map<String, Object> extraClaims,
		UserDetails userDetails,
		long expiration
	) {
		return Jwts.builder()
			.claims(extraClaims)
			.subject(userDetails.getUsername())
			.issuedAt(new Date(System.currentTimeMillis()))
			.expiration(new Date(System.currentTimeMillis() + expiration))
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

	public Long getRefreshExpirationTime() {
		return REFRESH_EXPIRATION_TIME;
	}

	public String mapExceptionToClientMessage(JwtException e) {
		return switch (e) {
			case ExpiredJwtException expiredJwtException -> "JWT expired";
			case SignatureException signatureException -> "Invalid JWT signature";
			case MalformedJwtException malformedJwtException -> "Malformed JWT";
			case null, default -> "Invalid token";
		};
	}
}
