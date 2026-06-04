package dev.mooka.translationnexus.repository;

import dev.mooka.translationnexus.domain.entity.TranslationEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TranslationRepository extends MongoRepository<TranslationEntity, String> {
    Optional<TranslationEntity> findByKeyCodeAndVersion(String keyCode, String version);
    boolean existsByKeyCodeAndVersion(String keyCode, String version);
    List<TranslationEntity> findAllByVersion(String version);
}
