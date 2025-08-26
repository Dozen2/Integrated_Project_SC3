package sit.int221.sc3_server.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.sc3_server.entity.Token;

public interface TokenRepository extends JpaRepository<Token, Integer> {
}