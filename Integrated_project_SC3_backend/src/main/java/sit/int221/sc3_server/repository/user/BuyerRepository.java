package sit.int221.sc3_server.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sit.int221.sc3_server.entity.Buyer;
import sit.int221.sc3_server.entity.VerifyToken;

import java.util.Optional;

public interface BuyerRepository extends JpaRepository<Buyer, Integer> {
    boolean existsBuyerByEmail(String email);

    @Query("""
    select p from Buyer p
    where (p.verifyToken = :token)
""")
    Optional<VerifyToken> findVerifyToken(
            @Param("token") String token
    );

    @Query("""
    select p from Buyer p
    where (p.fullName=:usernameOrEmail or p.email = :usernameOrEmail)
""")
Optional<Buyer> findByUserNameOrEmail(
        @Param("usernameOrEmail") String usernameOrEmail);
}