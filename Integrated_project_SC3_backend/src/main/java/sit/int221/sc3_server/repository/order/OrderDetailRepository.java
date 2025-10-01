package sit.int221.sc3_server.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.sc3_server.entity.OrderDetail;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
}