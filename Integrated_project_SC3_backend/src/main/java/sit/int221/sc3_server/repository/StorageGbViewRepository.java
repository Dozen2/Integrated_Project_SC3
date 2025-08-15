package sit.int221.sc3_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.sc3_server.entity.StorageGbView;

public interface StorageGbViewRepository extends JpaRepository<StorageGbView, Integer> {
}