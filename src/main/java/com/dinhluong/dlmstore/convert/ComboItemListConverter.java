package com.dinhluong.dlmstore.convert;




import com.dinhluong.dlmstore.dto.responses.ComboItemDetail;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Collections;
import java.util.List;

@Converter
public class ComboItemListConverter implements AttributeConverter<List<ComboItemDetail>, String> {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<ComboItemDetail> attribute) {
        try {
            return attribute == null ? null : mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error writing JSON", e);
        }
    }

    @Override
    public List<ComboItemDetail> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.trim().isEmpty()) return Collections.emptyList();
            return mapper.readValue(dbData, new TypeReference<List<ComboItemDetail>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Error reading JSON", e);
        }
    }
}
