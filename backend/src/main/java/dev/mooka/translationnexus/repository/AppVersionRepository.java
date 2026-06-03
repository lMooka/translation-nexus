package dev.mooka.translationnexus.repository;

import dev.mooka.translationnexus.domain.entity.AppVersionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AppVersionRepository extends MongoRepository<AppVersionEntity, String> {
    Optional<AppVersionEntity> findByActiveTrue();
    Optional<AppVersionEntity> findByVersion(String version);
    boolean existsByVersion(String version);
}
