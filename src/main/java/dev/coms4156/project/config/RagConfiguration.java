package dev.coms4156.project.config;

import io.micrometer.observation.ObservationRegistry;
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

/**
 * Configuration for RAG (Retrieval-Augmented Generation) functionality.
 * Using Spring AI with Ollama and PGVector.
 */
@Configuration
public class RagConfiguration {

  @Value("${spring.ai.ollama.base-url}")
  private String ollamaBaseUrl;

  /**
   * Creates OllamaApi bean for connecting to Ollama service.
   *
   * @return OllamaApi instance
   */
  @Bean
  public OllamaApi ollamaApi() {
    return new OllamaApi(ollamaBaseUrl);
  }

  /**
   * Creates OllamaOption bean for connecting to Ollama service.
   *
   * @return OllamaOption instance
   */
  @Bean
  public OllamaOptions ollamaOptions() {
    return OllamaOptions.create()
        .withModel("llama3.2")
        .withTemperature(0.7);
  }

  /**
   * Creates ObservationRegistry bean for connecting to Ollama service.
   *
   * @return ObservationRegistry instance
   */
  @Bean
  public ObservationRegistry observationRegistry() {
    return ObservationRegistry.create();
  }

  /**
   * Creates ModelManagementOptions bean for connecting to Ollama service.
   *
   * @return ModelManagementOptions instance
   */
  @Bean
  public ModelManagementOptions modelManagementOptions() {
    return ModelManagementOptions.builder().build();
  }

  /**
   * Creates ChatModel bean for connecting to Ollama service.
   *
   * @return OllamaChatModel instance
   */
  @Bean
  public ChatModel chatModel(OllamaApi ollamaApi, OllamaOptions ollamaOptions,
                             ObservationRegistry observationRegistry,
                             ModelManagementOptions modelManagementOptions) {
    return new OllamaChatModel(ollamaApi, ollamaOptions, null,
        null, observationRegistry, modelManagementOptions);
  }

  /**
   * Creates EmbeddingModel bean for connecting to Ollama service.
   *
   * @return EmbeddingModel instance
   */
  @Bean
  public EmbeddingModel embeddingModel(OllamaApi ollamaApi, OllamaOptions ollamaOptions,
                                       ObservationRegistry observationRegistry,
                                       ModelManagementOptions modelManagementOptions) {
    return new OllamaEmbeddingModel(ollamaApi, ollamaOptions, observationRegistry,
        modelManagementOptions);
  }

  /**
   * Creates VectorStore bean for connecting to Ollama service.
   *
   * @return VectorStore instance
   */
  @Bean
  public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
    return new PgVectorStore(jdbcTemplate, embeddingModel);
  }

  /**
   * Creates ChatClient bean for connecting to Ollama service.
   *
   * @return ChatClient instance
   */
  @Bean
  ChatClient chatClient(ChatModel chatModel) {
    return ChatClient.builder(chatModel).build();
  }
}
