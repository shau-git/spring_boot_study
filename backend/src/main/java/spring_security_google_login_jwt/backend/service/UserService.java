package spring_security_google_login_jwt.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import spring_security_google_login_jwt.backend.dto.FullUserProfileDto;
import spring_security_google_login_jwt.backend.dto.UserProfileDto;
import spring_security_google_login_jwt.backend.dto.UserProfileResponse;
import spring_security_google_login_jwt.backend.model.User;
import spring_security_google_login_jwt.backend.repo.UserRepo;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepo userRepository;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public String getEmailFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 1. Prevents NPE if run in @Async, JUnit tests, or early filters
        // 2. Returns null for standard web guests
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {  // if the user visit the route without log in, spring security will use anonymousUser (via AnonymousAuthenticationToken) to implement the Null Object Pattern. Instead of returning null when a user is not logged in, the framework provides a dummy Authentication object representing an unauthenticated guest.
            return null;
        }
        return authentication.getName();
    }

    public UserProfileDto getCurrentUserProfile() {
        // get current user's email from securityContext
        String currentEmail = this.getEmailFromSecurityContext();

        User user = this.findByEmail(currentEmail);

        log.info("yoyo user roles: {}", user.getRoles());
        return new UserProfileDto(user.getName(), user.getEmail(), user.getImage(),user.getRoles());
    }


    public UserProfileResponse getUserProfile(String targetEmail, Authentication authentication) {
        /*
        why check authentication != null :
        1. When a logged-in user visits: Your JwtAuthenticationFilter finds the valid JWT cookie, extracts the email, builds an Authentication object, and places it in the SecurityContext. Here, authentication is a valid object.

        2. When an unauthenticated guest visits: The guest has no JWT cookie. Your JwtAuthenticationFilter does nothing. SecurityContextHolder.getContext().getAuthentication() returns null.

        * */
        String currentEmail = this.getEmailFromSecurityContext();

        boolean isSelf = targetEmail.equalsIgnoreCase(currentEmail);
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        User user = this.findByEmail(currentEmail);

        // If the logged-in user is requesting their own profile or is an admin -> return FULL profile
        if (isSelf || isAdmin) {
            return new FullUserProfileDto(
                    user.getId(), user.getEmail(), user.getName(), user.getImage(),user.getRoles()
            );
        }

        // Otherwise -> return PUBLIC profile only
        return new UserProfileDto(
                user.getEmail(), user.getName(), user.getImage(),user.getRoles()
        );
    }
}
