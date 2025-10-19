package dev.coms4156.project.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class FloatArrayToPgVectorConverter implements AttributeConverter<float[], String> {

    @Override
    public String convertToDatabaseColumn(float[] attribute) {
        if (attribute == null || attribute.length == 0)
            return null;
        // Convert to Postgres vector format: [1.0, 2.0, 3.0]
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < attribute.length; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(attribute[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public float[] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty())
            return new float[0];
        String clean = dbData.replaceAll("[\\[\\]]", "");
        String[] parts = clean.split(",");
        float[] arr = new float[parts.length];
        for (int i = 0; i < parts.length; i++)
            arr[i] = Float.parseFloat(parts[i].trim());
        return arr;
    }
}
