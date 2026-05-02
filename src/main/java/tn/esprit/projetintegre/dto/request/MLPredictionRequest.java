package tn.esprit.projetintegre.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MLPredictionRequest {
    private String category;
    private String event_type;
    private String state;
    private int hour;
    private int month;
    private int day_of_week;
    private int duration_hours;
    private double price;
}
