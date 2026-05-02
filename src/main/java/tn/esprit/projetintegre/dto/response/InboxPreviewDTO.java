package tn.esprit.projetintegre.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InboxPreviewDTO {
    private String roomName;
    private String latestMessageContent;
    private String senderName;
    private LocalDateTime sentAt;
}
