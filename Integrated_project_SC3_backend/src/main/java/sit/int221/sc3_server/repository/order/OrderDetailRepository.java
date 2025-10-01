package sit.int221.sc3_server.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sit.int221.sc3_server.entity.OrderDetail;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    @Query("""
    select o from OrderDetail o
    where (:orderId is null or o.order.id = :orderId)
""")
    List<OrderDetail> findByOrderById(
            @Param("orderId") Integer orderId
    );
}