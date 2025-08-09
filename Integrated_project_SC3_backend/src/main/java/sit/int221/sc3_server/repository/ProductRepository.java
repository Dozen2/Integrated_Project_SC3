package sit.int221.sc3_server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sit.int221.sc3_server.entity.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    boolean existsByBrand_Id(int brandId);

    int countByBrand_Id(int id);

    Page<Product> findByBrand_NameIn(List<String> brandNames, Pageable pageable);
    boolean existsByModelIgnoreCase(String model);

    @Query("""
    select p from Product p
    WHERE (:brandName is null or p.brand.name in :brandName)
    and (:storageGb is null or p.storageGb in :storageGb)
    and (:minPrice is null or p.price >= :minPrice)
    and (:maxPrice is null or p.price <= :maxPrice)
""")
    Page<Product> findFilteredProduct(
            @Param("brandName") List<String> brandNames,
            @Param("storageGb") List<Integer> storageGb,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            Pageable pageable
    );

    @Query("""
    select p from Product p
    where (:brandName is null or p.brand.name in :brandName)
    and (:storageGb is null or p.storageGb in :storageGb)
""")
    Page<Product> findFilterProductNoPrice(
            @Param("brandName") List<String> brandNames,
            @Param("storageGb") List<Integer> storageGb,
            Pageable pageable
    );
}