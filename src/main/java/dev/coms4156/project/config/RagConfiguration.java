package dev.coms4156.project.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import io.micrometer.observation.ObservationRegistry;

/**
 * Configuration for RAG (Retrieval-Augmented Generation) functionality
 * using Spring AI with Ollama and PGVector
 */
@Configuration
public class RagConfiguration {

    @Value("${spring.ai.ollama.base-url}")
    private String ollamaBaseUrl;

    @Bean
    public OllamaApi ollamaApi() {
        return new OllamaApi(ollamaBaseUrl);
    }

    @Bean
    public OllamaOptions ollamaOptions() {
        return OllamaOptions.create()
                .withModel("llama3.2")
                .withTemperature(0.7);
    }

    @Bean
    public ObservationRegistry observationRegistry() {
        return ObservationRegistry.create();
    }

    @Bean
    public ModelManagementOptions modelManagementOptions() {
        return ModelManagementOptions.builder().build();
    }

    @Bean
    public ChatModel chatModel(OllamaApi ollamaApi, OllamaOptions ollamaOptions,
            ObservationRegistry observationRegistry, ModelManagementOptions modelManagementOptions) {
        return new OllamaChatModel(ollamaApi, ollamaOptions, null, null, observationRegistry, modelManagementOptions);
    }

    @Bean
    public EmbeddingModel embeddingModel(OllamaApi ollamaApi, OllamaOptions ollamaOptions,
            ObservationRegistry observationRegistry, ModelManagementOptions modelManagementOptions) {
        return new OllamaEmbeddingModel(ollamaApi, ollamaOptions, observationRegistry, modelManagementOptions);
    }

    @Bean
    public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return new PgVectorStore(jdbcTemplate, embeddingModel);
    }

    @Bean
    ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
