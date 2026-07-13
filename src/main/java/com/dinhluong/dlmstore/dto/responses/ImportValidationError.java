package com.dinhluong.dlmstore.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportValidationError {

    private String sheet;

    private int row;

    private String field;

    private String message;
}