package spring_security_google_login_jwt.backend.controllers;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import spring_security_google_login_jwt.backend.dto.UserProfileResponse;
import spring_security_google_login_jwt.backend.repo.UserRepo;
import spring_security_google_login_jwt.backend.dto.UserProfileDto;
import spring_security_google_login_jwt.backend.service.UserService;

import java.util.Map;

@RestController
@AllArgsConstructor
public class AuthController {

    private UserRepo userRepo;
    private UserService userService;

    @PostMapping("/api/auth/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false) // Set to 'true' in production
                .path("/")
                .maxAge(0)     // Immediately expires the cookie
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/user")
    public ResponseEntity<UserProfileDto> getCurrentUserProfile() {
        UserProfileDto profile = userService.getCurrentUserProfile();
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/profile/{targetEmail}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable String targetEmail, Authentication authentication) {
        UserProfileResponse profile = userService.getUserProfile(targetEmail, authentication);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/api/admin")
    public ResponseEntity<Map<String, String>> getInfo() {
        var map = Map.of("1", "ADMIN info");
        return ResponseEntity.ok(map);
    }

}
