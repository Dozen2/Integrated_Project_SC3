package sit.int221.sc3_server.repository.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sit.int221.sc3_server.entity.Order;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("""
    select o from Order o
    where (:sellerId is null or o.seller.id = :sellerId)
""")
    Page<Order> findOrderBySellerId(
            @Param("sellerId") Integer sellerId,
            Pageable pageable
    );

    @Query("""
select p from Order p 
where (:buyerId is null or p.buyer.id = :buyerId)
""")
    Page<Order> findByBuyerId(@Param("buyerId") Integer buyerId,Pageable pageable);
}