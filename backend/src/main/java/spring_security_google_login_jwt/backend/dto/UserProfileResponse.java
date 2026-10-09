package spring_security_google_login_jwt.backend.dto;

public sealed interface UserProfileResponse
        permits FullUserProfileDto, UserProfileDto {
}
/*
permits is used with a Java sealed class or sealed interface to explicitly list which classes are allowed to extend or implement it.

Only FullUserProfileDto and UserProfileDto are allowed to implement UserProfileResponse.


 */