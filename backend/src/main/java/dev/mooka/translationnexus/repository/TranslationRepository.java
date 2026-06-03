package dev.mooka.translationnexus.repository;

import dev.mooka.translationnexus.domain.TranslationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TranslationRepository extends MongoRepository<TranslationDocument, String> {
    Optional<TranslationDocument> findByKeyCodeAndVersion(String keyCode, String version);
    boolean existsByKeyCodeAndVersion(String keyCode, String version);
}
