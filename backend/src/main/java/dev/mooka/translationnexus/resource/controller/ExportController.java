package dev.mooka.translationnexus.resource.controller;

import dev.mooka.translationnexus.service.ExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    /**
     * Export approved translations as a ZIP file containing CSV files
     * divided by path mappings. MANAGER only.
     *
     * Example: GET /api/export?version=1.0.0
     */
    @GetMapping
    public void export(
            @RequestParam(required = false) String version,
            HttpServletResponse response) throws IOException {

        response.setContentType("application/zip");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"translations.zip\"");

        exportService.exportZip(version, response.getOutputStream());
    }
}
