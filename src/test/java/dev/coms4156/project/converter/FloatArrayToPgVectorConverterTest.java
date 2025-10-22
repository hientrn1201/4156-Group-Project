package dev.coms4156.project.converter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
}