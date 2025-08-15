package sit.int221.sc3_server.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "saleItemImage")
public class SaleItemImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 70)
    @NotNull
    @Column(name = "fileName", nullable = false, length = 70)
    private String fileName;

    @Column(name = "imageViewOrder")
    private Integer imageViewOrder;

    @Size(max = 50)
    @Column(name = "originalFileName", length = 50)
    private String originalFileName;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "saleItem_id", nullable = false)
    private SaleItem saleItem;

}