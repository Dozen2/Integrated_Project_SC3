package sit.int221.sc3_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sit.int221.sc3_server.entity.SaleItemImage;

import java.util.List;

public interface SaleItemImageRepository extends JpaRepository<SaleItemImage, Integer> {

    @Query("""
    select p.fileName from SaleItemImage p
    where p.originalFileName = :fileName
""")
    String findFileName(
            @Param("fileName") String fileName
    );
}