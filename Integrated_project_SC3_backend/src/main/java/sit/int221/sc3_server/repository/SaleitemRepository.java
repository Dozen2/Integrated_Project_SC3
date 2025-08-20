package sit.int221.sc3_server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sit.int221.sc3_server.entity.SaleItem;

import java.util.List;
import java.util.Optional;

public interface SaleitemRepository extends JpaRepository<SaleItem, Integer> {
    boolean existsByBrand_Id(int brandId);

    int countByBrand_Id(int id);

    Page<SaleItem> findByBrand_NameIn(List<String> brandNames, Pageable pageable);
    boolean existsByModelIgnoreCase(String model);

//    @Query("SELECT s FROM SaleItem s LEFT JOIN FETCH s.saleItemImages WHERE s.id = :id")
//    Optional<SaleItem> findByIdWithImages(@Param("id") Integer id);
@Query("""
    select p from SaleItem p
    WHERE (:brandNames IS NULL OR p.brand.name IN :brandNames)
    and (
         :storageGb IS NULL
         or (-1 in :storageGb and p.storageGb is null)
         or (p.storageGb in :storageGb and -1 not in :storageGb)
    )
    and (:minPrice IS NULL OR p.price >= :minPrice)
    and (:maxPrice IS NULL OR p.price <= :maxPrice)
    and (
        :searchValue IS NULL
        OR lower(p.model) like concat('%', :searchValue, '%')
        OR lower(cast(p.description as string)) like concat('%', :searchValue, '%')
        OR lower(p.color) like concat('%', :searchValue, '%')
    )
""")
Page<SaleItem> findFilteredProduct(
        @Param("brandNames") List<String> brandNames,
        @Param("storageGb") List<Integer> storageGb,
        @Param("minPrice") Integer minPrice,
        @Param("maxPrice") Integer maxPrice,
        @Param("searchValue") String searchValue,
        Pageable pageable
);


    @Query("""
    select p from SaleItem p
    WHERE (:brandNames is null or p.brand.name in :brandNames)
      and (
            :storageGb is null 
            or p.storageGb in :storageGb
            or p.storageGb IS NULL
          )
      and (:minPrice is null or p.price >= :minPrice)
      and (:maxPrice is null or p.price <= :maxPrice)
      and (
        :searchValue IS NULL
        OR lower(p.model) like concat('%', :searchValue, '%')
        OR lower(cast(p.description as string)) like concat('%', :searchValue, '%')
        OR lower(p.color) like concat('%', :searchValue, '%')
    )
""")
    Page<SaleItem> findFilteredProductAndNullStorageGb(
            @Param("brandNames") List<String> brandNames,
            @Param("storageGb") List<Integer> storageGb,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("searchValue") String searchValue,
            Pageable pageable
    );

    @Query("""
    select p from SaleItem p
    WHERE (:brandNames is null or p.brand.name in :brandNames)
      and (
            :storageGb is null
            or p.storageGb in :storageGb
            or p.storageGb IS NULL
          )
      and (:minPrice is null or p.price = :minPrice)
       and (
        :searchValue IS NULL
        OR lower(p.model) like concat('%', :searchValue, '%')
        OR lower(cast(p.description as string)) like concat('%', :searchValue, '%')
        OR lower(p.color) like concat('%', :searchValue, '%')
    )
""")
    Page<SaleItem> findFilteredProductAndNullStorageGbAndMinPrice(
            @Param("brandNames") List<String> brandNames,
            @Param("storageGb") List<Integer> storageGb,
            @Param("minPrice") Integer minPrice,
            @Param("searchValue") String searchValue,
            Pageable pageable
    );

//    @Query("""
//    select p from Saleitem p
//    where (:brandName is null or p.brand.name in :brandName)
//    and (:storageGb is null or p.storageGb in :storageGb)
//""")
//    Page<Saleitem> findFilterProductNoPrice(
//            @Param("brandName") List<String> brandNames,
//            @Param("storageGb") List<Integer> storageGb,
//            Pageable pageable
//    );
}