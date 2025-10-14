package sit.int221.sc3_server.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sit.int221.sc3_server.entity.Buyer;


import java.util.Optional;

public interface BuyerRepository extends JpaRepository<Buyer, Integer> {
    boolean existsBuyerByEmail(String email);

    @Query("""
                select p from Buyer p
                where (p.fullName=:usernameOrEmail or p.email = :usernameOrEmail)
            """)
    Optional<Buyer> findByUserNameOrEmail(
            @Param("usernameOrEmail") String usernameOrEmail);


    @Query("""
                select p from Buyer p
                where (p.email = :email and p.isActive  = false )
            """)
    Buyer findByEmailAndNonIsActive(
            @Param("email") String email);

    @Query("""
    select case when count(p) > 0 then true else false end
    from Buyer p
    where lower(p.email) = lower(:email)
      and p.isActive = :isActive
""")
    boolean existsByEmailAndIsActive(@Param("email") String email, @Param("isActive") boolean isActive);

    Optional<Buyer> findByEmail(String email);
}