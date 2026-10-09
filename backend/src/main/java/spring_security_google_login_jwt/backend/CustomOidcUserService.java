package spring_security_google_login_jwt.backend;

import org.springframework.security.core.GrantedAuthority;
import spring_security_google_login_jwt.backend.repo.UserRepo;
import spring_security_google_login_jwt.backend.model.User;
import spring_security_google_login_jwt.backend.model.Role;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CustomOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OidcUserService oidcUserService;
    private final UserRepo userRepo;

    public CustomOidcUserService(UserRepo userRepo) {
        this.oidcUserService = new OidcUserService();
        this.userRepo = userRepo;
    }

    @Override
    public @Nullable OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("Registration id: {}",registrationId); // google
        OidcUser oidcUser = this.oidcUserService.loadUser(userRequest);

        String name = oidcUser.getAttribute("name");
        String email = oidcUser.getAttribute("email");

        // if email is not found in our db, means is a new user (is a legit email as google send us the email), create the user in our db then fetch the User data back
        User user = userRepo.findByEmail(email).orElseGet(() -> userRepo.save(User.builder()
                .name(name)
                .email(email)
                .roles(Set.of(Role.ROLE_USER)) // default role is user
                .build()));
        log.info("User ini db {}", user); // User ini db User(id=1, name=Shau Jie K, email=k.shaujie@gmail.com, role=USER, image=null)
        log.info("name = {} , email = {}", name, email); // name = Shau Jie K , email = k.shaujie@gmail.com
        log.info("role is " + user.getRoles()); //OP: [ROLE_USER]
        log.info("oidcUser.getIdToken() =>" + oidcUser.getIdToken()); // OidcIdToken object representing the raw JWT ID token issued by Google.
        log.info("oidcUser.getClaims() =>" + oidcUser.getClaims()); // Returns a Map<String, Object> combining claims from both the ID Token and UserInfo.
        log.info("oidcUser.getAttributes() =>" + oidcUser.getAttributes()); // Returns a Map<String, Object> of user profile attributes

        // 2. Map Set<Role> to Set<GrantedAuthority> using Stream , SimpleGrantedAuthority implements GrantedAuthority
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getAuthority())  )
                .collect(Collectors.toSet()); // role.getAuthority() return 'ROLE_USER', OP: []

        // OidcUser is an interface, while DefaultOidcUser is a concrete class that implements that interface.
        return new DefaultOidcUser(
                authorities, // Your custom DB roles
                oidcUser.getIdToken(), // Original Google ID Token (representing the raw JWT ID token issued by Google.)
                oidcUser.getUserInfo() // Original Google User Info (email, name, picture) , this will return OidcUserInfo type
        );
        /*
        what will oidcUser.getUserInfo() return and method you can use.
        OidcUserInfo userInfo = oidcUser.getUserInfo();

        String email = userInfo.getEmail();          // Returns user's email
        String fullName = userInfo.getFullName();    // Returns user's full name
        String picture = userInfo.getPicture();      // Returns URL to Google profile picture
        String givenName = userInfo.getGivenName();  // Returns first name
        String familyName = userInfo.getFamilyName();// Returns last name
        Boolean emailVerified = userInfo.getEmailVerified(); // Returns email verification status
        
        Map<String, Object> allClaims = userInfo.getClaims(); // Returns raw map of all claims
         */
    }
}
