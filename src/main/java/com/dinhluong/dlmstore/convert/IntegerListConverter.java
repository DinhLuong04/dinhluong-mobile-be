package com.dinhluong.dlmstore.convert;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;
import java.util.Collections;

@Converter
public class IntegerListConverter implements AttributeConverter<List<Integer>, String> {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Integer> attribute) {
        try {
            return attribute == null ? null : mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error writing JSON", e);
        }
    }

    @Override
    public List<Integer> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null)
                return Collections.emptyList();
            return mapper.readValue(dbData, new TypeReference<List<Integer>>() {
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("Error reading JSON", e);
        }
    }
}