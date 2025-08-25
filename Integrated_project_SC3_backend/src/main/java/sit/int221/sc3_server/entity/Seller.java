package sit.int221.sc3_server.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "seller")
public class Seller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;


    @Size(max = 20)
    @NotNull
    @Column(name = "bankAccountNumber", nullable = false, length = 20)
    private String bankAccountNumber;

    @Size(max = 50)
    @NotNull
    @Column(name = "bankName", nullable = false, length = 50)
    private String bankName;

    @Size(max = 13)
    @NotNull
    @Column(name = "nationalId", nullable = false, length = 13)
    private String nationalId;

    @Size(max = 70)
    @NotNull
    @Column(name = "nationalIdPhotoFront", nullable = false, length = 70)
    private String nationalIdPhotoFront;

    @Size(max = 70)
    @NotNull
    @Column(name = "nationalIdPhotoBack", nullable = false, length = 70)
    private String nationalIdPhotoBack;

    @OneToOne(mappedBy = "seller")
    private User user;

    @Size(max = 10)
    @NotNull
    @Column(name = "mobileNumber", nullable = false, length = 10)
    private String mobileNumber;

}