package sit.int221.sc3_server.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sit.int221.sc3_server.entity.Seller;

import java.util.List;

public interface SellerRepository extends JpaRepository<Seller, Integer> {
    @Query("""
    select p.nationalIdPhotoBack,p.nationalIdPhotoFront from Seller p
    where (p.nationalIdPhotoFront = :frontName)
""")
    String findFileName(
            @Param("frontName") String frontName
    );

    @Query("""
    select s from Seller s
    where (s.id in :sellerId)
""")
    List<Seller> findAllSeller(@Param("sellerId") List<Integer> sellerId);

    boolean existsSellerByMobileNumber(String mobileNumber);

    boolean existsSellerByNationalId(String nationalId);
}