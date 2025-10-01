package sit.int221.sc3_server.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Buyer buyer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @Size(max = 150)
    @NotNull
    @Column(name = "shipping_address", nullable = false, length = 150)
    private String shippingAddress;

    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @NotNull
    @Column(name = "order_date", nullable = false)
    private Instant orderDate;

    @Size(max = 30)
    @NotNull
    @Column(name = "order_status", nullable = false, length = 30)
    private String orderStatus;

    @Size(max = 30)
    @NotNull
    @Column(name = "payment_status", nullable = false, length = 30)
    private String paymentStatus;

    @Size(max = 70)
    @Column(name = "order_note", length = 70)
    private String orderNote;

    @OneToMany(mappedBy = "order")
    private Set<OrderDetail> orderDetails = new LinkedHashSet<>();

}