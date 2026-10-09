package spring_security_google_login_jwt.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import spring_security_google_login_jwt.backend.model.User;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
}
