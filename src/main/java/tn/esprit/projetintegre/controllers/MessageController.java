package tn.esprit.projetintegre.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.request.MessageRequest;
import tn.esprit.projetintegre.dto.response.ChatRoomSuggestions;
import tn.esprit.projetintegre.dto.response.MessageResponse;
import tn.esprit.projetintegre.dto.response.RoomSentimentStats;
import tn.esprit.projetintegre.services.MessageService;
import tn.esprit.projetintegre.services.SmartSuggestionService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final SmartSuggestionService smartSuggestionService;

    // ── Send a message (REST fallback — prefer WebSocket for real-time) ─────────
    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(@Valid @RequestBody MessageRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Message envoyé avec succès", messageService.sendMessage(request)));
    }

    // ── Get a single message ────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MessageResponse>> getMessageById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(messageService.getMessageById(id)));
    }

    // ── Get chat room messages (paginated, newest first) ────────────────────────
    @GetMapping("/room/{chatRoomId}")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getMessagesByChatRoom(
            @PathVariable Long chatRoomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(messageService.getMessagesByChatRoom(chatRoomId, pageable)));
    }

    // ── Get full chat room history (ordered list) ───────────────────────────────
    @GetMapping("/room/{chatRoomId}/history")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getChatRoomHistory(@PathVariable Long chatRoomId) {
        return ResponseEntity.ok(ApiResponse.success(messageService.getChatRoomMessages(chatRoomId)));
    }

    // ── Get private conversation between two users ──────────────────────────────
    @GetMapping("/conversation")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getConversation(
            @RequestParam Long userId1,
            @RequestParam Long userId2) {
        return ResponseEntity.ok(ApiResponse.success(messageService.getConversation(userId1, userId2)));
    }

    // ── Edit a message ──────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MessageResponse>> editMessage(
            @PathVariable Long id,
            @RequestParam String content) {
        return ResponseEntity.ok(ApiResponse.success("Message modifié avec succès", messageService.editMessage(id, content)));
    }

    // ── Delete a message (soft delete) ─────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<MessageResponse>> deleteMessage(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Message supprimé avec succès", messageService.deleteMessage(id)));
    }

    // ── Mark conversation as read ───────────────────────────────────────────────
    @PutMapping("/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @RequestParam Long receiverId,
            @RequestParam Long senderId) {
        messageService.markConversationAsRead(receiverId, senderId);
        return ResponseEntity.ok(ApiResponse.success("Messages marqués comme lus", null));
    }

    // ── Admin: Get flagged messages (negative sentiment) ──────────────────────────
    @GetMapping("/admin/flagged-messages")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getFlaggedMessages() {
        return ResponseEntity.ok(ApiResponse.success(messageService.getFlaggedMessages()));
    }

    // ── Get room sentiment statistics ────────────────────────────────────────────
    @GetMapping("/room/{chatRoomId}/sentiment")
    public ResponseEntity<ApiResponse<RoomSentimentStats>> getRoomSentimentStats(@PathVariable Long chatRoomId) {
        return ResponseEntity.ok(ApiResponse.success(messageService.getRoomSentimentStats(chatRoomId)));
    }

    // ── ML-Driven Smart Suggestions for Room Owners ───────────────────────────────
    @GetMapping("/room/{chatRoomId}/suggestions")
    public ResponseEntity<ApiResponse<ChatRoomSuggestions>> getSmartSuggestions(@PathVariable Long chatRoomId) {
        return ResponseEntity.ok(ApiResponse.success(
                "AI-powered suggestions generated successfully",
                smartSuggestionService.generateSuggestions(chatRoomId)));
    }

    // ── Ping endpoint for testing throttling ───────────────────────────────────────
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "pong");
        response.put("userId", userId);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

}