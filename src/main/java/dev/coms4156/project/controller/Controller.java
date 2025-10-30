package dev.coms4156.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * Main REST controller for the Knowledge Management Service API.
 */
@RestController
@RequestMapping("/api")
@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = String.class)))
public class Controller {
  @GetMapping({"", "/"})
  public ResponseEntity<String> index() {
    return ResponseEntity.ok("Welcome to Knowledge Management Service Powered by AI!");
  }
}
