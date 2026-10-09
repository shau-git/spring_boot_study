package spring_security_google_login_jwt.backend.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import spring_security_google_login_jwt.backend.Utils.JwtUtils;
import spring_security_google_login_jwt.backend.model.User;
import spring_security_google_login_jwt.backend.repo.UserRepo;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepo userRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)  throws ServletException, IOException {

        String token = jwtUtils.parseJwt(request);;


        // 3. Validate token if present
        if (token != null && jwtUtils.validateToken(token)) {
            String email = jwtUtils.getEmailFromToken(token);

            // 1. Extract string roles directly from JWT claims
            List<String> roleClaims = jwtUtils.getRolesFromToken(token);

            // 2. Map claims to SimpleGrantedAuthority objects
            List<SimpleGrantedAuthority> authorities = roleClaims.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            // 3. Create authentication token
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 4. Set authentication into Spring SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

        }
            /*
        ----------------------
        String authHeader = request.getHeader("Authorization");

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtils.validateToken(token)) {
                String email = jwtUtils.getEmailFromToken(token);

                // Fetch user from DB and populate Security Context, it'll only proceed ifPresent() if the email is found (findByEmail() will return the Usr obj)
                userRepo.findByEmail(email).ifPresent(user -> {
                    Authentication auth =
                            new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            }

        }
        */


        filterChain.doFilter(request, response);
    }
}
/*
// why passing 2 arg in UsernamePasswordAuthenticationToken()
// 1. Unauthenticated constructor (2 arguments)
public UsernamePasswordAuthenticationToken(Object principal, Object credentials) {
    super(null);
    this.principal = principal;
    this.credentials = credentials;
    setAuthenticated(false); // Mark as NOT authenticated yet
}

// 2. Authenticated constructor (3 arguments)
public UsernamePasswordAuthenticationToken(Object principal, Object credentials,
                                           Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.principal = principal;
    this.credentials = credentials;
    super.setAuthenticated(true); // Mark as FULLY authenticated!
}

- When you pass 2 args (principal, credentials), Spring Security flags the object as authenticated = false. It assumes you are just capturing raw login input from a form, so it will pass this object to an AuthenticationManager to attempt validation.


- When you pass 3 args (principal, credentials, authorities), Spring Security flags the object as authenticated = true. You pass Collections.emptyList() (or user roles/authorities) to tell Spring: "This token is already verified by my custom JWT filter! Do not try to re-authenticate it."
 */


/*
Because you set SessionCreationPolicy.STATELESS in your SecurityConfig:
1. Spring Security does not save any session cookies (JSESSIONID).
2. Server memory is wiped clean of user state as soon as an HTTP response is sent back to React.
3. When React sends a new API request (e.g., GET /api/profile), Spring Security creates a brand-new, empty SecurityContext thread local for that request.

If JwtAuthenticationFilter tried to check if the user already existed in SecurityContext at the start of a request, it would always find null, because nothing has been stored yet!

Step:
[ React Client ]
       │  HTTP Request + "Authorization: Bearer <JWT>"
       ▼
┌────────────────────────────────────────────────────────────────────────┐
│                      JwtAuthenticationFilter                           │
│                                                                        │
│  1. READ: Extract "Bearer <JWT>" from Authorization header             │
│  2. VERIFY: Verify JWT signature & expiration using secret key          │
│  3. EXTRACT: Extract email from payload claim                          │
│  4. LOOKUP: Fetch User & Roles from DB via email                       │
│  5. POPULATE: Create UsernamePasswordAuthenticationToken(user, roles)  │
│     and store it in SecurityContextHolder                              │
└────────────────────────────────────────────────────────────────────────┘
       │
       ▼
┌────────────────────────────────────────────────────────────────────────┐
│                      Spring Authorization Filter                       │
│                                                                        │
│  • Reads SecurityContextHolder                                         │
│  • Sees valid Authentication object -> PASS                             │
│  • (If empty/null -> FAILS and sends HTTP 401 Unauthorized)            │
└────────────────────────────────────────────────────────────────────────┘
       │
       ▼
[ Controller / REST Endpoint ] ──> Sends HTTP 200 Response
       │
       ▼
[ REQUEST FINISHES ] ──> SecurityContext is cleared (Stateless)
 */