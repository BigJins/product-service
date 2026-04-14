package allmart.productservice.application.required;

import allmart.productservice.domain.event.OutboxEvent;
import org.springframework.data.repository.Repository;

public interface OutboxRepository extends Repository<OutboxEvent, Long> {
    OutboxEvent save(OutboxEvent event);
}
