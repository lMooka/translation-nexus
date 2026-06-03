package dev.mooka.translationnexus.repository;

import dev.mooka.translationnexus.domain.AppVersion;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AppVersionRepository extends MongoRepository<AppVersion, String> {
    Optional<AppVersion> findByActiveTrue();
    Optional<AppVersion> findByVersion(String version);
    boolean existsByVersion(String version);
}
