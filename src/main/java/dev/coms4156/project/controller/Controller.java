package dev.coms4156.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Main REST controller for the Knowledge Management Service API.
 */
@RestController
@RequestMapping("/api")
public class Controller {
  @GetMapping({"", "/"})
  public ResponseEntity<String> index() {
    return ResponseEntity.ok("Welcome to Knowledge Management Service Powered by AI!");
  }
}