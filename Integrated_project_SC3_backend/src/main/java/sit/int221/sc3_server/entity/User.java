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

    @Size(max = 100)
    @NotNull
    @Column(name = "passwords", nullable = false, length = 100)
    private String passwords;

    @Size(max = 50)
    @NotNull
    @Column(name = "fullName", nullable = false, length = 50)
    private String fullName;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "buyer_id", referencedColumnName = "id")
    private Buyer buyer;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "seller_id", referencedColumnName = "id")
    private Seller seller;


    @Column(name = "isActive")
    private Boolean isActive;



}