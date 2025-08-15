package sit.int221.sc3_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import sit.int221.sc3_server.entity.SaleItem;
import sit.int221.sc3_server.entity.SaleItemImage;

import java.util.List;
import java.util.Optional;

public interface SaleItemImageRepository extends JpaRepository<SaleItemImage, Integer> {

    @Query("""
    select p.fileName from SaleItemImage p
    where p.fileName = :fileName
""")
    String findFileName(
            @Param("fileName") String fileName
    );

    void deleteByFileNameAndSaleItem(String fileName, SaleItem saleItem);
    Optional<SaleItemImage> findByFileNameAndSaleItem(String fileName, SaleItem saleItem);
    @Modifying
    @Transactional
    @Query("DELETE FROM SaleItemImage s WHERE s.saleItem.id = :saleItemId")
    void deleteBySaleItemId(@Param("saleItemId") Integer saleItemId);
}