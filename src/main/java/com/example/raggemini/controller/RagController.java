package com.example.raggemini.controller;

import com.example.raggemini.service.DocumentService;
import com.example.raggemini.service.RagService;
import dev.langchain4j.data.document.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final DocumentService documentService;
    private final RagService ragService;

    public RagController(DocumentService documentService, RagService ragService) {
        this.documentService = documentService;
        this.ragService = ragService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Backend is running. Provider: " + ragService.getProvider()
                + ". API key loaded: " + ragService.isApiKeyLoaded());
    }

    @GetMapping("/provider")
    public ResponseEntity<Map<String, Object>> provider() {
        return ResponseEntity.ok(Map.of(
                "provider", ragService.getProvider(),
                "ready", ragService.isApiKeyLoaded(),
                "timestamp", Instant.now().toString()
        ));
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "chunking", defaultValue = "token") String chunkingTechnique) {
        
        try {
            Path tempDir = Files.createTempDirectory("rag_uploads");
            File tempFile = new File(tempDir.toFile(), file.getOriginalFilename());
            file.transferTo(tempFile);

            Document document = documentService.loadDocument(tempFile.toPath());
            ragService.ingestDocument(document);

            tempFile.delete();
            tempDir.toFile().delete();

            return ResponseEntity.ok("Successfully uploaded and processed " + file.getOriginalFilename() 
                    + " using " + chunkingTechnique + " technique.");
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to process file: " + e.getMessage());
        }
    }

    @PostMapping("/load-path")
    public ResponseEntity<String> loadFromPath(
            @RequestBody Map<String, String> payload) {
        String path = payload.get("path");
        String chunkingTechnique = payload.getOrDefault("chunking", "token");
        
        try {
            Path filePath = Path.of(path);
            if (!Files.exists(filePath)) {
                return ResponseEntity.badRequest().body("File not found at path: " + path);
            }
            
            Document document = documentService.loadDocument(filePath);
            ragService.ingestDocument(document);
            
            return ResponseEntity.ok("Successfully loaded and processed file from path using " 
                    + chunkingTechnique + " technique.");
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to process file: " + e.getMessage());
        }
    }

    @PostMapping("/ask")
    public ResponseEntity<String> ask(@RequestBody Map<String, String> payload) {
        String question = payload.get("question");
        if (question == null || question.isEmpty()) {
            return ResponseEntity.badRequest().body("Question cannot be empty");
        }
        
        try {
            String answer = ragService.ask(question);
            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error calling Gemini: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
}
