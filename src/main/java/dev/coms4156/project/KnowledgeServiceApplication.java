package dev.coms4156.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for the Knowledge Management Service.
 */
@SpringBootApplication
public final class KnowledgeServiceApplication {

  private KnowledgeServiceApplication() {
    // Private constructor to prevent instantiation
  }

  public static void main(String[] args) {
    SpringApplication.run(KnowledgeServiceApplication.class, args);
  }
}