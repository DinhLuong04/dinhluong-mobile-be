
package com.dinhluong.dlmstore.dto.responses;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class UserProfileStatsResponse {
    private String name;
    private String phone;
    private String rank;
    private String updateDate;
    private Integer orders;
    private BigDecimal money;
    private String nextRankMoney;
    private String nextRankName;
    private String startDate;
}
