package com.team.knowledge.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class Controller {
    @GetMapping({"/","/index"})
    public ResponseEntity<String> index() {
        return ResponseEntity.ok("Welcome to Knowledge Management Service Powered by AI!");
    }
}