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
    where (:buyerId is null or o.buyer.id = :buyerId)
""")
    Page<Order> findAllOrderByBuyerId(
            @Param("buyerId") Integer buyerId,
            Pageable pageable
    );
}