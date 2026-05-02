package tn.esprit.projetintegre.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomSentimentStats {
    private int totalMessages;
    private int analyzedMessages;
    private double averageScore;
    private String overallLabel;
    private int positiveCount;
    private int neutralCount;
    private int negativeCount;
}
