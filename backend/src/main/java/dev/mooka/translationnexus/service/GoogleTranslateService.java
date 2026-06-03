package dev.mooka.translationnexus.service;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translation;
import dev.mooka.translationnexus.exception.BusinessException;
import dev.mooka.translationnexus.exception.impl.GenericBusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GoogleTranslateService {

    @Autowired
    private Translate translate;

    public String translate(String text, String targetLanguage) throws BusinessException {
        if (translate == null) {
            throw new GenericBusinessException("Google Translate is currently unavailable (API key is not configured on the server).");
        }
        if (text == null || text.isBlank()) {
            return "";
        }
        try {
            Translation translation = translate.translate(
                    text,
                    Translate.TranslateOption.targetLanguage(targetLanguage)
            );
            return translation.getTranslatedText();
        } catch (Exception e) {
            log.error("Google Translate API error: ", e);
            throw new GenericBusinessException("Failed to translate text: " + e.getMessage());
        }
    }
}
