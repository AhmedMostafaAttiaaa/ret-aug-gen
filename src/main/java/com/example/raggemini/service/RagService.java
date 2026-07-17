package com.example.raggemini.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Service
public class RagService {

    @Value("${GEMINI_API_KEY}")
    private String geminiApiKey;

    private InMemoryEmbeddingStore<TextSegment> embeddingStore;
    private EmbeddingModel embeddingModel;
    private Assistant assistant;

    interface Assistant {
        String chat(String userMessage);
    }

    public boolean isApiKeyLoaded() {
        return geminiApiKey != null && !geminiApiKey.isEmpty() && !"your_gemini_api_key_here".equals(geminiApiKey);
    }

    @PostConstruct
    public void init() {
        System.out.println("=== RAG SERVICE INIT ===");
        System.out.println("API Key present: " + isApiKeyLoaded());
        System.out.println("API Key length: " + (geminiApiKey != null ? geminiApiKey.length() : 0));
        if (geminiApiKey != null && geminiApiKey.length() > 8) {
            System.out.println("API Key starts with: " + geminiApiKey.substring(0, 8) + "...");
        }

        if (!isApiKeyLoaded()) {
            System.err.println("WARNING: GEMINI_API_KEY is not set correctly in .env!");
            return;
        }

        try {
            // Initialize Embedding Store
            embeddingStore = new InMemoryEmbeddingStore<>();

            // Initialize Embedding Model
            embeddingModel = GoogleAiEmbeddingModel.builder()
                    .apiKey(geminiApiKey)
                    .modelName("text-embedding-004")
                    .build();

            // Initialize Chat Model
            ChatLanguageModel chatModel = GoogleAiGeminiChatModel.builder()
                    .apiKey(geminiApiKey)
                    .modelName("gemini-1.5-flash")
                    .build();

            // Create Content Retriever
            ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                    .embeddingStore(embeddingStore)
                    .embeddingModel(embeddingModel)
                    .maxResults(5)
                    .minScore(0.5)
                    .build();

            // Create Assistant
            assistant = AiServices.builder(Assistant.class)
                    .chatLanguageModel(chatModel)
                    .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                    .contentRetriever(contentRetriever)
                    .build();

            System.out.println("=== RAG SERVICE INITIALIZED SUCCESSFULLY ===");
        } catch (Exception e) {
            System.err.println("=== RAG SERVICE INIT FAILED ===");
            e.printStackTrace();
        }
    }

    public void ingestDocument(Document document) {
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        ingestor.ingest(document);
    }

    public String ask(String question) {
        if (assistant == null) {
            return "Assistant is not initialized properly. Check your GEMINI_API_KEY.";
        }
        return assistant.chat(question);
    }
}
