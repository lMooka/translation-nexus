package dev.mooka.translationnexus.repository;

import dev.mooka.translationnexus.domain.entity.LocaleEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocaleRepository extends MongoRepository<LocaleEntity, String> {
}
