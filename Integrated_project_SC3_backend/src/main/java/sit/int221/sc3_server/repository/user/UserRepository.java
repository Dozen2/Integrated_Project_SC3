package sit.int221.sc3_server.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sit.int221.sc3_server.entity.User;
import sit.int221.sc3_server.entity.VerifyToken;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    boolean existsUserByEmail(String email);

    @Query("""
    select p from User p
    where (p.verifyTokens = :token)
""")
    Optional<VerifyToken> findVerifyToken(
            @Param("token") String token
    );
}