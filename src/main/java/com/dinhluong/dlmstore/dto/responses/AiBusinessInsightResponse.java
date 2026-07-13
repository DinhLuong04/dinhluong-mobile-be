package com.dinhluong.dlmstore.dto.responses;



import lombok.Data;
import java.util.List;

@Data
public class AiBusinessInsightResponse {
    
    private String executiveSummary; // Tóm tắt ngắn gọn 2-3 câu
    
    private FinancialInsight financialInsight;
    private SalesAndProduct salesAndProduct;
    private CustomerService customerService;
    private List<RiskAlert> riskAlerts;
    private List<ActionableAdvice> actionableAdvices;

    @Data
    public static class FinancialInsight {
        private String trend; // Trả về "UP", "DOWN", "STABLE" để FE map icon
        private String analysis;
    }

    @Data
    public static class SalesAndProduct {
        private String topPerformersAnalysis;
        private String crossSellOpportunities; // Khai thác từ combo
    }

    @Data
    public static class CustomerService {
        private String satisfactionAnalysis;
        private String cancellationInsights; // Phân tích lý do hủy từ cột reason
    }

    @Data
    public static class RiskAlert {
        private String severity; // "HIGH", "MEDIUM", "LOW"
        private String issue;
        private String recommendation;
    }

    @Data
    public static class ActionableAdvice {
        private String title;
        private String detail;
        private String expectedImpact; // Ví dụ: "Tăng 15% tỷ lệ chuyển đổi"
    }
}
