package com.example.raggemini.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

@Service
public class DocumentService {

    private final DocumentParser parser = new ApacheTikaDocumentParser();

    public Document loadDocument(Path filePath) {
        return FileSystemDocumentLoader.loadDocument(filePath, parser);
    }

    public List<TextSegment> chunkDocument(Document document, String technique) {
        DocumentSplitter splitter;
        switch (technique.toLowerCase()) {
            case "paragraph":
                splitter = DocumentSplitters.recursive(1000, 100);
                break;
            case "sentence":
                splitter = DocumentSplitters.recursive(200, 20);
                break;
            case "token":
            default:
                splitter = DocumentSplitters.recursive(500, 50);
                break;
        }
        return splitter.split(document);
    }
}
