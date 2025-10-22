package dev.coms4156.project.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DocumentRelationshipTest {

  @Test
  void testEqualsAndHashCode() {
    DocumentRelationship rel1 = new DocumentRelationship();
    rel1.setId(1L);
    rel1.setRelationshipType(DocumentRelationship.RelationshipType.SEMANTIC_SIMILARITY);
    
    DocumentRelationship rel2 = new DocumentRelationship();
    rel2.setId(1L);
    rel2.setRelationshipType(DocumentRelationship.RelationshipType.SEMANTIC_SIMILARITY);
    
    DocumentRelationship rel3 = new DocumentRelationship();
    rel3.setId(2L);
    rel3.setRelationshipType(DocumentRelationship.RelationshipType.TOPICAL_RELATEDNESS);
    
    assertEquals(rel1, rel2);
    assertEquals(rel1.hashCode(), rel2.hashCode());
    assertNotEquals(rel1, rel3);
    assertNotEquals(rel1, null);
    assertNotEquals(rel1, "string");
    assertEquals(rel1, rel1);
  }

  @Test
  void testToString() {
    DocumentRelationship rel = new DocumentRelationship();
    rel.setId(1L);
    rel.setRelationshipType(DocumentRelationship.RelationshipType.SEMANTIC_SIMILARITY);
    
    String result = rel.toString();
    assertNotNull(result);
    assertTrue(result.contains("DocumentRelationship"));
  }

  @Test
  void testEquals_NullFields() {
    DocumentRelationship rel1 = new DocumentRelationship();
    DocumentRelationship rel2 = new DocumentRelationship();
    
    assertEquals(rel1, rel2);
    
    rel1.setId(null);
    rel2.setId(1L);
    assertNotEquals(rel1, rel2);
  }

  @Test
  void testHashCode_NullFields() {
    DocumentRelationship rel = new DocumentRelationship();
    rel.setId(null);
    rel.setSimilarityScore(null);
    
    int hash = rel.hashCode();
    assertNotNull(hash);
  }
}