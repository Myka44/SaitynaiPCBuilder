package lt.komplektatorius.configuration;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BuildConfigurationRepository extends JpaRepository<BuildConfiguration, UUID>,
        JpaSpecificationExecutor<BuildConfiguration> {
    @EntityGraph(attributePaths = {"owner", "components"})
    Optional<BuildConfiguration> findWithComponentsById(UUID id);

    long countByComponentsId(UUID componentId);
}

