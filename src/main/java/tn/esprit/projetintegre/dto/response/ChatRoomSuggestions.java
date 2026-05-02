package tn.esprit.projetintegre.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Wrapper for all ML-driven suggestions for a chat room.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomSuggestions {
    
    private Long chatRoomId;
    private String chatRoomName;
    private LocalDateTime generatedAt;
    
    // Overall health metrics
    private Double overallHealthScore; // 0.0 to 1.0
    private String healthStatus; // HEALTHY, WARNING, CRITICAL
    private String healthTrend; // IMPROVING, DECLINING, STABLE
    
    // Categorized suggestions
    private List<SmartSuggestion> highPriority;
    private List<SmartSuggestion> mediumPriority;
    private List<SmartSuggestion> lowPriority;
    
    // Counts
    private Integer totalSuggestions;
    private Integer urgentCount;
    private Integer memberActionsCount;
    private Integer moderationActionsCount;
    private Integer businessOpportunitiesCount;
    
    // Summary insights
    private List<String> keyInsights;
    private String recommendedNextAction;
    
    // Historical comparison
    private Double previousHealthScore;
    private Double healthScoreChange;
    
    // Risk assessment
    private String riskLevel; // NONE, LOW, MEDIUM, HIGH, CRITICAL
    private List<String> riskFactors;
}
