package sit.int221.sc3_server.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import sit.int221.sc3_server.entity.Buyer;
import sit.int221.sc3_server.entity.Seller;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "nickName", nullable = false, length = 50)
    private String nickName;

    @Size(max = 70)
    @NotNull
    @Column(name = "email", nullable = false, length = 70)
    private String email;

    @Size(max = 50)
    @NotNull
    @Column(name = "passwords", nullable = false, length = 50)
    private String passwords;

    @Size(max = 50)
    @NotNull
    @Column(name = "fullName", nullable = false, length = 50)
    private String fullName;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Buyer buyer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

}