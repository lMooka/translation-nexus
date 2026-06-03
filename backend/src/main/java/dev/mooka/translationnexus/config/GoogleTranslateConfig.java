package dev.mooka.translationnexus.config;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class GoogleTranslateConfig {

    @Bean
    public Translate GoogleTranslateService(@Value("${google.api.key}") String googleApiKey) {
        Translate translate;
        if (googleApiKey != null && !googleApiKey.isBlank()) {
            translate = TranslateOptions.newBuilder()
                    .setApiKey(googleApiKey)
                    .build()
                    .getService();
            log.info("Google Translate Service initialized using API key.");
        } else {
            translate = null;
            log.warn("GOOGLE_API_KEY environment variable is not set. Google Translate will be unavailable.");
        }

        return translate;
    }
}
