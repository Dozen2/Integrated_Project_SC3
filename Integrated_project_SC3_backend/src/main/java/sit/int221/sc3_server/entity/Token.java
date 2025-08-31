package sit.int221.sc3_server.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import sit.int221.sc3_server.entity.Buyer;

import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "tokens")
public class    Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @Column(name = "access_token", length = 100)
    private String accessToken;

    @Size(max = 100)
    @Column(name = "refresh_token", length = 100)
    private String refreshToken;

    @Column(name = "accessToken_expireDate")
    private LocalTime accesstokenExpiredate;

    @Column(name = "refreshToken_expireDate")
    private LocalTime refreshtokenExpiredate;

    @OneToOne
    @JoinColumn(name = "buyer_id", nullable = false, unique = true) // 🔹 unique เพื่อให้ buyer มี token เดียว
    private Buyer buyer;
}
