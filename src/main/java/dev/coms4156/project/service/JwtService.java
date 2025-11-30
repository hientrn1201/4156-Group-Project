package dev.coms4156.project.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for JWT token generation and validation.
 */
@Service
public class JwtService {

  @Value(
      "${app.jwt.secret:your-256-bit-secret-key-change-this-in-production-minimum-32-characters}"
  )
  private String secretKey;

  @Value("${app.jwt.expiration:86400000}") // 24 hours default
  private Long expiration;

  /**
   * Generates a JWT token for a user.
   *
   * @param username the username
   * @param userId   the user ID
   * @param role     the user role
   * @return the JWT token
   */
  public String generateToken(String username, Long userId, String role) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userId);
    claims.put("role", role);
    return createToken(claims, username);
  }

  /**
   * Creates a JWT token with the given claims and subject.
   *
   * @param claims  the claims to include in the token
   * @param subject the subject (username)
   * @return the JWT token
   */
  private String createToken(Map<String, Object> claims, String subject) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expiration);

    return Jwts.builder()
        .claims(claims)
        .subject(subject)
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(getSigningKey())
        .compact();
  }

  /**
   * Extracts the username from a JWT token.
   *
   * @param token the JWT token
   * @return the username
   */
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * Extracts the user ID from a JWT token.
   *
   * @param token the JWT token
   * @return the user ID
   */
  public Long extractUserId(String token) {
    return extractClaim(token, claims -> claims.get("userId", Long.class));
  }

  /**
   * Extracts the role from a JWT token.
   *
   * @param token the JWT token
   * @return the role
   */
  public String extractRole(String token) {
    return extractClaim(token, claims -> claims.get("role", String.class));
  }

  /**
   * Extracts a claim from a JWT token.
   *
   * @param token          the JWT token
   * @param claimsResolver function to extract the claim
   * @param <T>            the type of the claim
   * @return the claim value
   */
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  /**
   * Extracts all claims from a JWT token.
   *
   * @param token the JWT token
   * @return the claims
   */
  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  /**
   * Validates a JWT token.
   *
   * @param token    the JWT token
   * @param username the username to validate against
   * @return true if the token is valid, false otherwise
   */
  public boolean validateToken(String token, String username) {
    final String tokenUsername = extractUsername(token);
    return (tokenUsername.equals(username) && !isTokenExpired(token));
  }

  /**
   * Checks if a JWT token is expired.
   *
   * @param token the JWT token
   * @return true if expired, false otherwise
   */
  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  /**
   * Extracts the expiration date from a JWT token.
   *
   * @param token the JWT token
   * @return the expiration date
   */
  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  /**
   * Gets the signing key for JWT tokens.
   *
   * @return the signing key
   */
  private SecretKey getSigningKey() {
    byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
