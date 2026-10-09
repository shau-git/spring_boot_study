package spring_security_google_login_jwt.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfiguration;
import spring_security_google_login_jwt.backend.CustomOidcUserService;
import spring_security_google_login_jwt.backend.OAuth2SuccessHandler;
import spring_security_google_login_jwt.backend.filters.JwtAuthenticationFilter;


import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomOidcUserService customOidcUserService;

    @Autowired
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private static final String[] PUBLIC_URLS = {
            "/oauth2/**",
            "/login/**",
            "/api/user",
            "/api/auth/logout",
            "/profile/**",

            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-ui.html",
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. ENABLE CORS , so your frontend can access th
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                // 1. Make session state stateless (no JSESSIONID created)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/admin").hasAnyRole("ADMIN")
                        .anyRequest().authenticated()
                )

                // 2. Handle Google OAuth2 login & redirect with JWT, executed in OAuth2LoginAuthenticationFilter
                .oauth2Login(oauth2 -> oauth2
                        /*
                        userInfoEndpoint() tell spring security to use our own custom OidcUserService, if exclude this:

                         1) CustomOidcUserService.loadUser() will NEVER run: Spring Security defaults back to its built-in OidcUserService implementation.

                         2) Users will NOT be synced or saved to your database: New users logging in via Google will not trigger userRepo.save(...).

                        3) Custom DB Roles will be lost: Spring Security will assign default Google OIDC scopes to the principal instead of your local database roles (ROLE_USER, etc.).

                        4) OAuth2SuccessHandler behavior will degrade: When OAuth2SuccessHandler executes, authentication.getPrincipal() will contain Google's default OidcUser instead of your enriched DefaultOidcUser.

                        */

                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(customOidcUserService))
                        .successHandler(oAuth2SuccessHandler)
                )
                /*
                how does your .oauth2Login() works:
                OAuth2LoginAuthenticationFilter
                       │
                       ▼
                AuthenticationManager
                       │
                       ▼
                OAuth2LoginAuthenticationProvider (or OidcAuthorizationCodeAuthenticationProvider)
                       │
                       ├── 1.User logs in with google, and the credentials are verified by google
                       │      └── if authentication is fine then will proceed
                       │
                       ├── 2 Calls your CustomOidcUserService.loadUser(userRequest)
                       │      └── Returns your DefaultOidcUser
                       │
                       ├── 3. Constructs OAuth2AuthenticationToken:
                       │      new OAuth2AuthenticationToken(oidcUser, oidcUser.getAuthorities(), clientRegId)
                       │
                       ▼
                OAuth2SuccessHandler.onAuthenticationSuccess(..., authentication)


                The Exact Spring Security Source Code for OidcAuthorizationCodeAuthenticationProvider, right after executing your CustomOidcUserService, Spring performs this exact instantiation:
                // Spring Security internal execution:
                OidcUser oidcUser = this.userService.loadUser(userRequest); // Calls your CustomOidcUserService

                Collection<? extends GrantedAuthority> authorities = oidcUser.getAuthorities();

                // This is where OAuth2AuthenticationToken is instantiated!
                OAuth2AuthenticationToken authenticationToken = new OAuth2AuthenticationToken(
                    oidcUser,
                    authorities,
                    userRequest.getClientRegistration().getRegistrationId()
                );

                return authenticationToken; // Passed directly into OAuth2SuccessHandler
                 */

                // 3. Add JWT filter to validate Bearer tokens on protected API calls
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Explicit origin required (cannot use "*")
        configuration.setAllowedOrigins(List.of(frontendUrl));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "Authorization"));

        // Allows browser to include cookies in cross-origin requests
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
