package spring_security_google_login_jwt.backend.Utils;


import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;
import spring_security_google_login_jwt.backend.model.Role;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;


@Component
public class JwtUtils {

    @Value("${jwt.secret:YourSuperSecretKeyThatIsAtLeast32BytesLong!}")
    private String jwtSecret;

    @Value("${jwt.expirationMs:86400000}") // 24 hours
    private int jwtExpirationMs;

    private  final String JWT_COOKIE_NAME = "jwt";

    public String generateToken(String email, Set<Role> roles) {
        // Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        List<String> authorityStrings = roles.stream()
                .map(Role::getAuthority)
                .toList();

        return Jwts.builder()
                .setSubject(email)
                .claim("roles", authorityStrings) // Packed as array e.g., ["ROLE_USER", "ROLE_ADMIN"]
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(this.getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmailFromToken(String token) {

        return Jwts.parser()
                .verifyWith(this.getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }


    public boolean validateToken(String token) {
        try {
            //SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parser()
                    .verifyWith(this.getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }


    public String parseJwt(HttpServletRequest request) {
        // 1. Try extracting from HttpOnly cookie first
        String cookieJwt = this.getJwtFromCookie(request);
        if (cookieJwt != null && !cookieJwt.isBlank()) {
            return cookieJwt;
        }

        // 2. Fallback to Extract token from Authorization Header: Bearer token
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }

    /**
     * Extracts the JWT string from the request's HttpOnly cookie.
     * Returns null if the cookie is not present.
     */
    public String getJwtFromCookie(HttpServletRequest request) {

        Cookie cookie = WebUtils.getCookie(request, JWT_COOKIE_NAME);
        if (cookie != null) {
            return cookie.getValue();
        }
        return null;
    }

    /*
    // What Spring's WebUtils.getCookie() actually does internally:
    public static Cookie getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    */

    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(this.getSigningKey()) // SecretKey instance used for signing
                .build()
                .parseSignedClaims(token)
                .getPayload();

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);

        return roles != null ? roles : Collections.emptyList();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}