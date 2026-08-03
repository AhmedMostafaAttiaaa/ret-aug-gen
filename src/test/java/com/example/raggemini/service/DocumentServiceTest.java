package com.example.raggemini.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentServiceTest {

    private final DocumentService documentService = new DocumentService();

    @Test
    void tokenTechniqueProducesNonEmptySegments() {
        Document document = Document.from("word ".repeat(300));

        List<TextSegment> segments = documentService.chunkDocument(document, "token");

        assertThat(segments).isNotEmpty();
    }

    @Test
    void unknownTechniqueFallsBackToTokenDefault() {
        Document document = Document.from("word ".repeat(300));

        List<TextSegment> defaultSegments = documentService.chunkDocument(document, "token");
        List<TextSegment> unknownSegments = documentService.chunkDocument(document, "not-a-real-technique");

        assertThat(unknownSegments).hasSameSizeAs(defaultSegments);
    }

    @Test
    void nullTechniqueDoesNotThrow() {
        Document document = Document.from("word ".repeat(300));

        List<TextSegment> segments = documentService.chunkDocument(document, null);

        assertThat(segments).isNotEmpty();
    }

    @Test
    void sentenceTechniqueProducesSmallerChunksThanParagraph() {
        Document document = Document.from("word ".repeat(300));

        List<TextSegment> sentenceSegments = documentService.chunkDocument(document, "sentence");
        List<TextSegment> paragraphSegments = documentService.chunkDocument(document, "paragraph");

        assertThat(sentenceSegments.size()).isGreaterThanOrEqualTo(paragraphSegments.size());
    }

    @Test
    void loadDocumentReadsPlainTextFile(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("sample.txt");
        Files.writeString(file, "hello from a test file");

        Document document = documentService.loadDocument(file);

        assertThat(document.text()).contains("hello from a test file");
    }
}
