package sit.int221.sc3_server.repository.saleItem;

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


    @Query("""
select p from SaleItem p
where (:sellerId is null or p.seller.id = :sellerId)
and (:saleItemId is null or p.id = :saleItemId)
""")
    SaleItem findBySellerId(
            @Param("sellerId") Integer sellerId,
            @Param("saleItemId") Integer saleItemId
    );
    @Query("""
select p from SaleItem p
where (p.seller.id in :sellerId)
""")
    Page<SaleItem> findSaleItemBySellerId(
            @Param("sellerId") int id,
            Pageable pageable
    );
@Query("""
    select p from SaleItem p
    WHERE (:sellerId is null or p.seller.id = :sellerId)
    and   (:brandNames IS NULL OR p.brand.name IN :brandNames)
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
        @Param("sellerId") Integer sellerId,
        @Param("brandNames") List<String> brandNames,
        @Param("storageGb") List<Integer> storageGb,
        @Param("minPrice") Integer minPrice,
        @Param("maxPrice") Integer maxPrice,
        @Param("searchValue") String searchValue,
        Pageable pageable
);


    @Query("""
    select p from SaleItem p
    WHERE (:sellerId is null or p.seller.id = :sellerId)
    and (:brandNames is null or p.brand.name in :brandNames)
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
            @Param("sellerId") Integer sellerId,
            @Param("brandNames") List<String> brandNames,
            @Param("storageGb") List<Integer> storageGb,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("searchValue") String searchValue,
            Pageable pageable
    );

    @Query("""
    select p from SaleItem p
    WHERE (:sellerId is null or p.seller.id = :sellerId)
    and (:brandNames is null or p.brand.name in :brandNames)
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
            @Param("sellerId") Integer sellerId,
            @Param("brandNames") List<String> brandNames,
            @Param("storageGb") List<Integer> storageGb,
            @Param("minPrice") Integer minPrice,
            @Param("searchValue") String searchValue,
            Pageable pageable
    );


}