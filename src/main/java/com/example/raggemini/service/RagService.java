package com.example.raggemini.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

@Service
public class RagService {

    private static final Set<String> KNOWN_PROVIDERS = Set.of("gemini", "openai", "ollama", "anthropic");

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

    @Value("${anthropic.api-key}")
    private String anthropicApiKey;
    @Value("${anthropic.chat-model}")
    private String anthropicChatModel;

    @Value("${embedding-store.path}")
    private String embeddingStorePath;

    private InMemoryEmbeddingStore<TextSegment> embeddingStore;
    private EmbeddingModel embeddingModel;
    private Assistant assistant;
    private StreamingAssistant streamingAssistant;

    interface Assistant {
        String chat(String userMessage);
    }

    interface StreamingAssistant {
        TokenStream chat(String userMessage);
    }

    public String getProvider() {
        return provider;
    }

    public boolean isApiKeyLoaded() {
        return switch (provider) {
            case "openai" -> openAiApiKey != null && !openAiApiKey.isEmpty();
            case "ollama" -> true; // no API key required for local Ollama
            case "anthropic" -> anthropicApiKey != null && !anthropicApiKey.isEmpty();
            default -> geminiApiKey != null && !geminiApiKey.isEmpty() && !"your_gemini_api_key_here".equals(geminiApiKey);
        };
    }

    @PostConstruct
    public void init() {
        System.out.println("=== RAG SERVICE INIT ===");
        System.out.println("LLM provider: " + provider);
        System.out.println("Ready: " + isApiKeyLoaded());

        if (!KNOWN_PROVIDERS.contains(provider)) {
            throw new IllegalStateException("Unknown llm.provider '" + provider
                    + "'. Expected one of " + KNOWN_PROVIDERS + ".");
        }

        if (!isApiKeyLoaded()) {
            System.err.println("WARNING: No API key configured for provider '" + provider + "'. Check your .env file.");
            return;
        }

        try {
            File storeFile = new File(embeddingStorePath);
            if (storeFile.exists()) {
                embeddingStore = InMemoryEmbeddingStore.fromFile(storeFile.toPath());
                System.out.println("Loaded persisted embedding store from " + embeddingStorePath);
            } else {
                embeddingStore = new InMemoryEmbeddingStore<>();
            }

            ChatLanguageModel chatModel;
            StreamingChatLanguageModel streamingChatModel;
            switch (provider) {
                case "openai" -> {
                    var chatBuilder = OpenAiChatModel.builder()
                            .apiKey(openAiApiKey)
                            .modelName(openAiChatModel);
                    var streamingBuilder = OpenAiStreamingChatModel.builder()
                            .apiKey(openAiApiKey)
                            .modelName(openAiChatModel);
                    if (openAiBaseUrl != null && !openAiBaseUrl.isEmpty()) {
                        chatBuilder.baseUrl(openAiBaseUrl);
                        streamingBuilder.baseUrl(openAiBaseUrl);
                    }
                    chatModel = chatBuilder.build();
                    streamingChatModel = streamingBuilder.build();

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

                    streamingChatModel = OllamaStreamingChatModel.builder()
                            .baseUrl(ollamaBaseUrl)
                            .modelName(ollamaChatModel)
                            .build();

                    embeddingModel = OllamaEmbeddingModel.builder()
                            .baseUrl(ollamaBaseUrl)
                            .modelName(ollamaEmbeddingModel)
                            .build();
                }
                case "anthropic" -> {
                    chatModel = AnthropicChatModel.builder()
                            .apiKey(anthropicApiKey)
                            .modelName(anthropicChatModel)
                            .build();

                    streamingChatModel = AnthropicStreamingChatModel.builder()
                            .apiKey(anthropicApiKey)
                            .modelName(anthropicChatModel)
                            .build();

                    // Anthropic has no embeddings API; embed locally instead.
                    embeddingModel = new AllMiniLmL6V2EmbeddingModel();
                }
                default -> {
                    chatModel = GoogleAiGeminiChatModel.builder()
                            .apiKey(geminiApiKey)
                            .modelName(geminiChatModel)
                            .build();

                    streamingChatModel = GoogleAiGeminiStreamingChatModel.builder()
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

            // Separate chat memory from the non-streaming assistant above, since the two
            // are independent conversational threads (REST ask vs. SSE stream endpoints).
            streamingAssistant = AiServices.builder(StreamingAssistant.class)
                    .streamingChatLanguageModel(streamingChatModel)
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
        embeddingStore.serializeToFile(Path.of(embeddingStorePath));
    }

    public String ask(String question) {
        if (assistant == null) {
            return "Assistant is not initialized properly. Check your configured LLM provider and API key.";
        }
        return assistant.chat(question);
    }

    public TokenStream askStream(String question) {
        if (streamingAssistant == null) {
            throw new IllegalStateException("Assistant is not initialized properly. Check your configured LLM provider and API key.");
        }
        return streamingAssistant.chat(question);
    }
}
