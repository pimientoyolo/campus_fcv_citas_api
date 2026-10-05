package co.fcv.citas.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationEventJpaRepository extends JpaRepository<NotificationEventEntity, Long> {
    List<NotificationEventEntity> findByStatusOrderByCreatedAtAsc(String status);
}
