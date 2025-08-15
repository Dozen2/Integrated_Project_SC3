package sit.int221.sc3_server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

/**
 * Mapping for DB view
 */
@Getter
@Setter
@Entity
@Immutable
@Table(name = "storagegb_view")
public class StorageGbView {
    @Id
    @Column(name = "storageGb")
    private Integer storageGb;

    //TODO [JPA Buddy] generate columns from DB
}