package tn.esprit.projetintegre.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.projetintegre.enums.SuggestionType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ML-driven suggestion for chat room owners based on sentiment analysis.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmartSuggestion {
    
    private SuggestionType type;
    private String title;
    private String description;
    private String priority; // HIGH, MEDIUM, LOW
    private double confidenceScore; // 0.0 to 1.0
    private double severityScore; // 0.0 to 1.0
    
    // Context data
    private String triggeredBy; // What condition triggered this
    private Double sentimentScore;
    private Integer affectedMessageCount;
    private LocalDateTime detectedAt;
    
    // Target user (for member-specific suggestions)
    private Long targetUserId;
    private String targetUserName;
    private Integer targetUserNegativeCount;
    private Double targetUserSentimentScore;
    
    // Recommended actions
    private List<String> recommendedActions;
    
    // Trend data
    private String trendDirection; // IMPROVING, DECLINING, STABLE
    private Double trendChangePercent;
    
    // Time-based context
    private String timeOfDay; // MORNING, AFTERNOON, EVENING, NIGHT
    private Boolean isUrgent;
    
    // Stats at detection time
    private Integer totalMessages;
    private Integer positiveCount;
    private Integer negativeCount;
    private Integer neutralCount;
}
