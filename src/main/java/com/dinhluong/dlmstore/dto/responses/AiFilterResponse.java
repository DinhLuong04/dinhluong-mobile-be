package com.dinhluong.dlmstore.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiFilterResponse {
    private String answer;
    private List<Long> productIds; // AI sẽ trả về list ID này để ta lọc
}