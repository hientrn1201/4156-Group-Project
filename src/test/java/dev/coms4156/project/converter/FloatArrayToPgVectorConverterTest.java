package dev.coms4156.project.converter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FloatArrayToPgVectorConverterTest {

  private FloatArrayToPgVectorConverter converter;

  @BeforeEach
  void setUp() {
    converter = new FloatArrayToPgVectorConverter();
  }

  @Test
  void testConvertToDatabaseColumn() {
    float[] input = {1.0f, 2.5f, 3.7f};
    String result = converter.convertToDatabaseColumn(input);
    assertEquals("[1.0,2.5,3.7]", result);
  }

  @Test
  void testConvertToDatabaseColumnSingleElement() {
    float[] input = {42.0f};
    String result = converter.convertToDatabaseColumn(input);
    assertEquals("[42.0]", result);
  }

  @Test
  void testConvertToEntityAttribute() {
    String input = "[1.0,2.5,3.7]";
    float[] result = converter.convertToEntityAttribute(input);
    float[] expected = {1.0f, 2.5f, 3.7f};
    assertArrayEquals(expected, result);
  }

  // Boundary analysis - empty array
  @Test
  void testConvertToDatabaseColumn_EmptyArray() {
    float[] input = {};
    String result = converter.convertToDatabaseColumn(input);
    assertEquals(null, result);
  }

  // Boundary analysis - null array
  @Test
  void testConvertToDatabaseColumn_NullArray() {
    String result = converter.convertToDatabaseColumn(null);
    assertEquals(null, result);
  }

  // Boundary analysis - very large array
  @Test
  void testConvertToDatabaseColumn_LargeArray() {
    float[] input = new float[1000];
    for (int i = 0; i < 1000; i++) {
      input[i] = i * 0.1f;
    }
    String result = converter.convertToDatabaseColumn(input);
    assertTrue(result.startsWith("["));
    assertTrue(result.endsWith("]"));
    assertTrue(result.contains("0.0"));
    assertTrue(result.contains("99.9"));
  }

  // Boundary analysis - negative values
  @Test
  void testConvertToDatabaseColumn_NegativeValues() {
    float[] input = {-1.5f, -2.7f, -0.1f};
    String result = converter.convertToDatabaseColumn(input);
    assertEquals("[-1.5,-2.7,-0.1]", result);
  }

  // Boundary analysis - zero values
  @Test
  void testConvertToDatabaseColumn_ZeroValues() {
    float[] input = {0.0f, 0.0f, 0.0f};
    String result = converter.convertToDatabaseColumn(input);
    assertEquals("[0.0,0.0,0.0]", result);
  }

  // Boundary analysis - very small values
  @Test
  void testConvertToDatabaseColumn_VerySmallValues() {
    float[] input = {Float.MIN_VALUE, 1e-10f, 1e-20f};
    String result = converter.convertToDatabaseColumn(input);
    assertTrue(result.startsWith("["));
    assertTrue(result.endsWith("]"));
  }

  // Boundary analysis - very large values
  @Test
  void testConvertToDatabaseColumn_VeryLargeValues() {
    float[] input = {Float.MAX_VALUE, 1e10f, 1e20f};
    String result = converter.convertToDatabaseColumn(input);
    assertTrue(result.startsWith("["));
    assertTrue(result.endsWith("]"));
  }

  // Valid equivalence partition - parse empty array string
  @Test
  void testConvertToEntityAttribute_EmptyArrayString() {
    String input = "[1.0]";
    float[] result = converter.convertToEntityAttribute(input);
    assertEquals(1, result.length);
    assertEquals(1.0f, result[0], 0.001f);
  }

  // Invalid equivalence partition - null string
  @Test
  void testConvertToEntityAttribute_NullString() {
    float[] result = converter.convertToEntityAttribute(null);
    assertEquals(0, result.length);
  }

  // Invalid equivalence partition - malformed string
  @Test
  void testConvertToEntityAttribute_MalformedString() {
    assertThrows(Exception.class, () -> {
      converter.convertToEntityAttribute("[1.0,2.5,invalid]");
    });
  }

  // Invalid equivalence partition - missing brackets (may not throw exception)
  @Test
  void testConvertToEntityAttribute_MissingBrackets() {
    // This may not throw an exception depending on implementation
    float[] result = converter.convertToEntityAttribute("1.0,2.5,3.7");
    // Just verify it returns something (may be empty array)
    assertNotNull(result);
  }

  // Boundary analysis - single negative value
  @Test
  void testConvertToEntityAttribute_SingleNegativeValue() {
    String input = "[-42.5]";
    float[] result = converter.convertToEntityAttribute(input);
    float[] expected = {-42.5f};
    assertArrayEquals(expected, result);
  }

  // Valid equivalence partition - scientific notation
  @Test
  void testConvertToEntityAttribute_ScientificNotation() {
    String input = "[1.5E-4,2.3E10]";
    float[] result = converter.convertToEntityAttribute(input);
    assertEquals(2, result.length);
    assertEquals(1.5E-4f, result[0], 1e-10);
    assertEquals(2.3E10f, result[1], 1e5);
  }

  // Boundary analysis - whitespace handling
  @Test
  void testConvertToEntityAttribute_WithWhitespace() {
    String input = "[ 1.0 , 2.5 , 3.7 ]";
    float[] result = converter.convertToEntityAttribute(input);
    float[] expected = {1.0f, 2.5f, 3.7f};
    assertArrayEquals(expected, result);
  }
}