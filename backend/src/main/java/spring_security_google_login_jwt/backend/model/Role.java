package spring_security_google_login_jwt.backend.model;
import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ROLE_USER,
    ROLE_ADMIN,
    ROLE_MANAGER;

    @Override
    public String getAuthority() {
        return name();
    }
}
