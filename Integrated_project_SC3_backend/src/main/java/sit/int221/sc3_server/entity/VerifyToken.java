package sit.int221.sc3_server.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "verifyToken")
public class VerifyToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @Column(name = "verify_token", length = 100)
    private String verifyToken;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "expiredDate", nullable = false)
    private Instant expiredDate;

    @Override
    public String toString() {
        return "VerifyToken{" +
                "id=" + id +
                ", verifyToken='" + verifyToken + '\'' +
                ", userId=" + (user != null ? user.getId() : "null") +
                ", expiredDate=" + expiredDate +
                '}';
    }
}