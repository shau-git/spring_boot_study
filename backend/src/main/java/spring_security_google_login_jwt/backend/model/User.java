package spring_security_google_login_jwt.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="users")
@Data
/*
@Getter
@Setter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable=false, unique=true)
    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    /*
    // adding @Builder.Default , means even if you dont set the roles in builder it will remain as [] instead of null
    User user = User.builder()
                .name("Shau Jie")
                .build();

    System.out.println(user.getRoles()); // Outputs: [] (empty HashSet, NOT null)
    */

    private String image;
}

