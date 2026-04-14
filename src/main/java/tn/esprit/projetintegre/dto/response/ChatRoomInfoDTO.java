package tn.esprit.projetintegre.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomInfoDTO {
    private String roomName;
    private String creatorUsername;
    private Long memberCount;
}
