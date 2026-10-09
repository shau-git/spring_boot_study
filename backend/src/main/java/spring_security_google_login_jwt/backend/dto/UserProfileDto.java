package spring_security_google_login_jwt.backend.dto;

import spring_security_google_login_jwt.backend.model.Role;

import java.util.Set;

public record UserProfileDto (
    String email,
    String name,
    String image,
    Set<Role> roles
)implements UserProfileResponse{}
