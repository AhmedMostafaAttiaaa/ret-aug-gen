package com.example.raggemini.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class RagService {

    @Value("${llm.provider}")
    private String provider;

    @Value("${gemini.api-key}")
    private String geminiApiKey;
    @Value("${gemini.chat-model}")
    private String geminiChatModel;
    @Value("${gemini.embedding-model}")
    private String geminiEmbeddingModel;

    @Value("${openai.api-key}")
    private String openAiApiKey;
    @Value("${openai.base-url}")
    private String openAiBaseUrl;
    @Value("${openai.chat-model}")
    private String openAiChatModel;
    @Value("${openai.embedding-model}")
    private String openAiEmbeddingModel;

    @Value("${ollama.base-url}")
    private String ollamaBaseUrl;
    @Value("${ollama.chat-model}")
    private String ollamaChatModel;
    @Value("${ollama.embedding-model}")
    private String ollamaEmbeddingModel;

    private InMemoryEmbeddingStore<TextSegment> embeddingStore;
    private EmbeddingModel embeddingModel;
    private Assistant assistant;

    interface Assistant {
        String chat(String userMessage);
    }

    public boolean isApiKeyLoaded() {
        return switch (provider) {
            case "openai" -> openAiApiKey != null && !openAiApiKey.isEmpty();
            case "ollama" -> true; // no API key required for local Ollama
            default -> geminiApiKey != null && !geminiApiKey.isEmpty() && !"your_gemini_api_key_here".equals(geminiApiKey);
        };
    }

    @PostConstruct
    public void init() {
        System.out.println("=== RAG SERVICE INIT ===");
        System.out.println("LLM provider: " + provider);
        System.out.println("Ready: " + isApiKeyLoaded());

        if (!isApiKeyLoaded()) {
            System.err.println("WARNING: No API key configured for provider '" + provider + "'. Check your .env file.");
            return;
        }

        try {
            embeddingStore = new InMemoryEmbeddingStore<>();

            ChatLanguageModel chatModel;
            switch (provider) {
                case "openai" -> {
                    var chatBuilder = OpenAiChatModel.builder()
                            .apiKey(openAiApiKey)
                            .modelName(openAiChatModel);
                    if (openAiBaseUrl != null && !openAiBaseUrl.isEmpty()) {
                        chatBuilder.baseUrl(openAiBaseUrl);
                    }
                    chatModel = chatBuilder.build();

                    var embeddingBuilder = OpenAiEmbeddingModel.builder()
                            .apiKey(openAiApiKey)
                            .modelName(openAiEmbeddingModel);
                    if (openAiBaseUrl != null && !openAiBaseUrl.isEmpty()) {
                        embeddingBuilder.baseUrl(openAiBaseUrl);
                    }
                    embeddingModel = embeddingBuilder.build();
                }
                case "ollama" -> {
                    chatModel = OllamaChatModel.builder()
                            .baseUrl(ollamaBaseUrl)
                            .modelName(ollamaChatModel)
                            .build();

                    embeddingModel = OllamaEmbeddingModel.builder()
                            .baseUrl(ollamaBaseUrl)
                            .modelName(ollamaEmbeddingModel)
                            .build();
                }
                default -> {
                    chatModel = GoogleAiGeminiChatModel.builder()
                            .apiKey(geminiApiKey)
                            .modelName(geminiChatModel)
                            .build();

                    embeddingModel = GoogleAiEmbeddingModel.builder()
                            .apiKey(geminiApiKey)
                            .modelName(geminiEmbeddingModel)
                            .build();
                }
            }

            ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                    .embeddingStore(embeddingStore)
                    .embeddingModel(embeddingModel)
                    .maxResults(5)
                    .minScore(0.5)
                    .build();

            assistant = AiServices.builder(Assistant.class)
                    .chatLanguageModel(chatModel)
                    .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                    .contentRetriever(contentRetriever)
                    .build();

            System.out.println("=== RAG SERVICE INITIALIZED SUCCESSFULLY (" + provider + ") ===");
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
            return "Assistant is not initialized properly. Check your configured LLM provider and API key.";
        }
        return assistant.chat(question);
    }
}
