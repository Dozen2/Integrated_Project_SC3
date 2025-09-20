package sit.int221.sc3_server.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import sit.int221.sc3_server.utils.Role;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "buyer")
public class Buyer {
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", unique = true) // 🔹 unique เพื่อบังคับ 1-1
    private Seller seller;

    @Column(name = "isActive")
    private Boolean isActive;


    @OneToOne(mappedBy = "buyer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private VerifyToken verifyToken; // 🔹 Buyer มี VerifyToken เดียว

    @ElementCollection(fetch = FetchType.EAGER)   // บังคับโหลด roles มาพร้อม Buyer
    @Enumerated(EnumType.STRING)                 // เก็บค่า enum เป็น String (อ่านง่าย)
    @CollectionTable(
            name = "buyer_roles",                    // ตั้งชื่อตาราง (optional)
            joinColumns = @JoinColumn(name = "buyer_id")
    )
    @Column(name = "role")                       // ตั้งชื่อ column (optional)
    private Set<Role> roles = new HashSet<>();
}
