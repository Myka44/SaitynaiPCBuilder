package lt.komplektatorius.component;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ComponentRepository extends JpaRepository<Component, UUID>, JpaSpecificationExecutor<Component> {
}

