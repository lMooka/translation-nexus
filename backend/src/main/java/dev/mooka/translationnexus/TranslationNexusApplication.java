package dev.mooka.translationnexus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class TranslationNexusApplication {
    public static void main(String[] args) {
        SpringApplication.run(TranslationNexusApplication.class, args);
    }
}
