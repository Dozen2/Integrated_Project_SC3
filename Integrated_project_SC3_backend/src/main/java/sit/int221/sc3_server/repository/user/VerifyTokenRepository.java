package sit.int221.sc3_server.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.sc3_server.entity.VerifyToken;

public interface VerifyTokenRepository extends JpaRepository<VerifyToken, Integer> {
    VerifyToken findByVerifyToken(String token);
}