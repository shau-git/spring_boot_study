package spring_security_google_login_jwt.backend;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import spring_security_google_login_jwt.backend.Utils.JwtUtils;
import spring_security_google_login_jwt.backend.model.Role;
import spring_security_google_login_jwt.backend.model.User;
import spring_security_google_login_jwt.backend.repo.UserRepo;

import java.io.IOException;
import java.util.Set;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Autowired
    private UserRepo userRepository; // Your JPA repository

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        // 1. Extract email from Google OidcUser / OAuth2User
        // authentication will be the return value from .userInfoEndpoint(userInfo -> userInfo.oidcUserService(customOidcUserService)) in spring security.java
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        // 2. Fetch user from DB (or create a new user with default ROLE_USER if first login)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User verified by OAuth2 but not found in DB: " + email));
        // 1. Generate your custom JWT
        String token = jwtUtils.generateToken(user.getEmail(), user.getRoles());

        // 2. Redirect to React with token in URL parameter
        /*
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/redirect")
                .queryParam("token", token)
                .build().toUriString();*/

        // after the oauth done, your browswer url will change from localhost:8080//login/oauth2/code/google to localhost:5173/oauth2/redirect?token=ey... , then in your frontend code you should capture this token?ey... and save the token in localstorage then change the url to the /dashboard etc

        // 2. Build HTTP-Only SameSite Cookie
        String targetUrl = frontendUrl + "/home";
        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)                // Blocks JavaScript from reading the cookie (prevents XSS)
                .secure(false)                 // Set to 'true' in production when using HTTPS
                .path("/")                     // Cookie sent on all backend endpoints
                .maxAge(24 * 60 * 60)          // Expiration in seconds (e.g., 1 day)
                .sameSite("Lax")               // Controls cross-site cookie behavior
                .build();

        // 3. Add Set-Cookie header to HTTP response
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // 4. Clean Redirect to React (No tokens in the URL!)

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

/*
.oauth2Login() in Spring Security, this filter automatically registers itself and listens for requests matching /oauth2/authorization/*. When a request hits /oauth2/authorization/google:
1) The filter looks up your client-id, scopes, and redirect-uri from application.yml.
2) It generates a secure, random state token to prevent CSRF attacks.
3) Tt constructs the long Google URL ([https://accounts.google.com/o/oauth2/v2/auth?client_id=...&state=](https://accounts.google.com/o/oauth2/v2/auth?client_id=...&state=)...) and sends back an HTTP 302 Redirect to the browser. (it will change the domain to the issuerer-uri from the application.yml


Note: in Google Cloud Console, you registered an Authorized Redirect URI: http://localhost:8080/login/oauth2/code/google  , so once it done authentication will redirect to this page


Spring Security uses two distinct internal filters for the Google OAuth2 flow:
1) OAuth2AuthorizationRequestRedirectFilter (Initiation):
    - Triggers when you go to /oauth2/authorization/google.
    - Redirects the browser to Google's login page.
2) OAuth2LoginAuthenticationFilter (Completion):
    - Triggers when Google redirects back to /login/oauth2/code/google.
    - Exchanges Google's authorization code for tokens, runs your CustomOidcUserService, and calls your OAuth2SuccessHandler



 */








