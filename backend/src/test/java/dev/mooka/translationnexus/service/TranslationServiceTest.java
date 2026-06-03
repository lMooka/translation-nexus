package dev.mooka.translationnexus.service;

import dev.mooka.translationnexus.domain.TranslationDocument;
import dev.mooka.translationnexus.domain.TranslationValue;
import dev.mooka.translationnexus.domain.AppVersion;
import dev.mooka.translationnexus.repository.AppVersionRepository;
import dev.mooka.translationnexus.domain.Category;
import dev.mooka.translationnexus.domain.PathMapping;
import dev.mooka.translationnexus.repository.CategoryRepository;
import dev.mooka.translationnexus.repository.TranslationRepository;
import dev.mooka.translationnexus.resource.dto.ImportResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TranslationServiceTest {

    @Mock
    private TranslationRepository translationRepository;

    @Mock
    private AppVersionRepository appVersionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private TranslationService translationService;

    private byte[] csvContent;

    @BeforeEach
    void setUp() {
        String csv = "Review Status,Category,Record Display Name,Internal Name,Field,English,Example Translation,Português,Weblate Key\n" +
                "Approved,General,Welcome Screen,welcome_title,Name,Welcome to Translation Nexus,,Bem-vindo ao Translation Nexus,welcome.title\n" +
                "Needs Translation,Gameplay,Quest Log,quest_desc,Description,Find the magic herb,,Herba,quest.herb.desc\n" +
                "Approved,UI,Close Button,close_btn,Label,Close,,,button.close\n"; // last row: Portuguese is blank but Approved -> should skip pt entry and add to skipped keys
        csvContent = csv.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void testImportCsv_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csvContent);
        
        AppVersion mockActiveVersion = AppVersion.builder()
                .version("1.0")
                .active(true)
                .build();
        when(appVersionRepository.findByActiveTrue()).thenReturn(Optional.of(mockActiveVersion));
        when(categoryRepository.findByNameIgnoreCase("General")).thenReturn(Optional.of(Category.builder().name("General").pathMappings(List.of(new PathMapping("*", "general.csv"))).build()));
        when(categoryRepository.findByNameIgnoreCase("Gameplay")).thenReturn(Optional.of(Category.builder().name("Gameplay").pathMappings(List.of(new PathMapping("quest.*.desc", "quest_descriptions.csv"))).build()));
        when(categoryRepository.findByNameIgnoreCase("UI")).thenReturn(Optional.of(Category.builder().name("UI").pathMappings(List.of(new PathMapping("button.*", "buttons.csv"))).build()));
        when(translationRepository.findByKeyCodeAndVersion(any(), any())).thenReturn(Optional.empty());

        ImportResultDTO result = translationService.importCsv(file, "test-user");

        assertNotNull(result);
        assertEquals(3, result.totalRows());
        assertEquals(3, result.importedCount());
        assertEquals(1, result.skippedCount()); // because last row is Approved with blank Portuguese
        assertTrue(result.skippedKeyCodes().contains("button.close"));

        // Verify saveAll was called with the imported documents
        ArgumentCaptor<List<TranslationDocument>> captor = ArgumentCaptor.forClass(List.class);
        verify(translationRepository).saveAll(captor.capture());
        
        List<TranslationDocument> savedDocs = captor.getValue();
        assertEquals(3, savedDocs.size());

        // Check first document (welcome.title)
        TranslationDocument doc1 = savedDocs.stream()
                .filter(d -> "welcome.title".equals(d.getKeyCode()))
                .findFirst()
                .orElseThrow();
        assertEquals("General", doc1.getCategory());
        assertEquals("Welcome to Translation Nexus", doc1.getBaseValue());
        assertTrue(doc1.getTags().contains("entity:welcome_title"));
        assertTrue(doc1.getTags().contains("field:Name"));
        
        TranslationValue en1 = doc1.getTranslations().get("en");
        assertNotNull(en1);
        assertEquals("Welcome to Translation Nexus", en1.getTranslatedValue());
        assertEquals("APPROVED", en1.getStatus());

        TranslationValue pt1 = doc1.getTranslations().get("pt");
        assertNotNull(pt1);
        assertEquals("Bem-vindo ao Translation Nexus", pt1.getTranslatedValue());
        assertEquals("APPROVED", pt1.getStatus());

        // Check second document (quest.herb.desc) - status Needs Translation -> pt should be PENDING_APPROVAL
        TranslationDocument doc2 = savedDocs.stream()
                .filter(d -> "quest.herb.desc".equals(d.getKeyCode()))
                .findFirst()
                .orElseThrow();
        assertEquals("Gameplay", doc2.getCategory());
        assertEquals("Find the magic herb", doc2.getBaseValue());
        
        TranslationValue pt2 = doc2.getTranslations().get("pt");
        assertNotNull(pt2);
        assertEquals("Herba", pt2.getTranslatedValue());
        assertEquals("REVIEW", pt2.getStatus());

        // Check third document (button.close) - blank Portuguese, Approved -> pt entry should NOT exist
        TranslationDocument doc3 = savedDocs.stream()
                .filter(d -> "button.close".equals(d.getKeyCode()))
                .findFirst()
                .orElseThrow();
        assertEquals("UI", doc3.getCategory());
        assertEquals("Close", doc3.getBaseValue());
        assertFalse(doc3.getTranslations().containsKey("pt"));
    }
}
