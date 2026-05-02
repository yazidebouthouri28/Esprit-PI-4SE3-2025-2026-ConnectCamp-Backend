package tn.esprit.projetintegre.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReactionRequest {
    @NotNull(message = "L'ID du message est obligatoire")
    private Long messageId;

    @NotNull(message = "L'ID de l'utilisateur est obligatoire")
    private Long userId;

    @NotBlank(message = "L'emoji est obligatoire")
    private String emoji;
    
    private Long chatRoomId; // to broadcast correctly
}
