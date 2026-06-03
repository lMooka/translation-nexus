package dev.mooka.translationnexus.service;

import dev.mooka.translationnexus.domain.Locale;
import dev.mooka.translationnexus.exception.BusinessException;
import dev.mooka.translationnexus.exception.impl.GenericBusinessException;
import dev.mooka.translationnexus.repository.LocaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocaleService {

    private final LocaleRepository localeRepository;

    public List<Locale> getAllLocales() {
        return localeRepository.findAll();
    }

    public Optional<Locale> getLocaleById(String id) {
        if (id == null) return Optional.empty();
        return localeRepository.findById(id.trim().toLowerCase());
    }

    public Locale createLocale(Locale locale) throws BusinessException {
        if (locale == null || locale.getId() == null || locale.getId().isBlank()) {
            throw new GenericBusinessException("Locale code (ID) cannot be empty");
        }
        if (locale.getName() == null || locale.getName().isBlank()) {
            throw new GenericBusinessException("Locale name cannot be empty");
        }
        String cleanId = locale.getId().trim().toLowerCase();
        if (localeRepository.existsById(cleanId)) {
            throw new GenericBusinessException("Locale code '" + cleanId + "' already exists");
        }
        Locale doc = Locale.builder()
                .id(cleanId)
                .name(locale.getName().trim())
                .googleCode(locale.getGoogleCode() != null ? locale.getGoogleCode().trim() : null)
                .build();
        log.info("Locale created: {}", cleanId);
        return localeRepository.save(doc);
    }

    public Locale updateLocale(String id, Locale locale) throws BusinessException {
        Locale existing = localeRepository.findById(id)
                .orElseThrow(() -> new GenericBusinessException(404, "exception.locale.not-found", "Locale not found"));
        if (locale.getName() == null || locale.getName().isBlank()) {
            throw new GenericBusinessException("Locale name cannot be empty");
        }
        existing.setName(locale.getName().trim());
        existing.setGoogleCode(locale.getGoogleCode() != null ? locale.getGoogleCode().trim() : null);
        log.info("Locale updated: {}", id);
        return localeRepository.save(existing);
    }

    public void deleteLocale(String id) throws BusinessException {
        Locale existing = localeRepository.findById(id)
                .orElseThrow(() -> new GenericBusinessException(404, "exception.locale.not-found", "Locale not found"));
        localeRepository.delete(existing);
        log.info("Locale deleted: {}", id);
    }
}
