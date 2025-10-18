package dev.coms4156.project.controller;

import dev.coms4156.project.model.Document;
import dev.coms4156.project.repository.DocumentRepository;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

  private final DocumentRepository docStore;

  public DocumentController(DocumentRepository docStore) {
    this.docStore = docStore;
  }

  @PostMapping("/test")
  public Document createTest(@RequestParam String filename) {
    Document d = new Document();
    d.setFilename(filename);
    d.setContentType("text/plain");
    d.setFileSize(0L);
    d.setExtractedText("testing");
    return docStore.save(d);
  }

  @GetMapping("/list")
  public List<Document> list() {
    return docStore.findAll();
  }

}


