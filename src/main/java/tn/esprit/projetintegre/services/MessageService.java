package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.dto.request.MessageRequest;
import tn.esprit.projetintegre.dto.response.MessageResponse;
import tn.esprit.projetintegre.dto.response.RoomSentimentStats;
import tn.esprit.projetintegre.entities.ChatRoom;
import tn.esprit.projetintegre.entities.Message;
import tn.esprit.projetintegre.entities.MessageReaction;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.repositories.ChatRoomRepository;
import tn.esprit.projetintegre.repositories.MessageReactionRepository;
import tn.esprit.projetintegre.repositories.MessageRepository;
import tn.esprit.projetintegre.repositories.UserRepository;
import tn.esprit.projetintegre.services.SentimentAnalysisService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageReactionRepository messageReactionRepository;
    private final SentimentAnalysisService sentimentAnalysisService;

    // ── Send a message ──────────────────────────────────────────────────────────
    public MessageResponse sendMessage(MessageRequest request) {
        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found with ID: " + request.getSenderId()));

        User receiver = null;
        if (request.getReceiverId() != null) {
            receiver = userRepository.findById(request.getReceiverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Receiver not found with ID: " + request.getReceiverId()));
        }

        ChatRoom chatRoom = null;
        if (request.getChatRoomId() != null) {
            chatRoom = chatRoomRepository.findById(request.getChatRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("ChatRoom not found with ID: " + request.getChatRoomId()));
        }

        Message replyTo = null;
        if (request.getReplyToId() != null) {
            replyTo = messageRepository.findById(request.getReplyToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Replied message not found with ID: " + request.getReplyToId()));
        }

        // Analyze sentiment of the message content
        SentimentAnalysisService.SentimentResult sentiment = sentimentAnalysisService.analyzeSentiment(request.getContent());

        Message message = Message.builder()
                .content(request.getContent())
                .messageType(request.getMessageType())
                .mediaUrl(request.getMediaUrl())
                .fileName(request.getFileName())
                .fileSize(request.getFileSize())
                .sentAt(LocalDateTime.now())
                .sender(sender)
                .receiver(receiver)
                .chatRoom(chatRoom)
                .replyTo(replyTo)
                .isRead(false)
                .isEdited(false)
                .isDeleted(false)
                .sentimentScore(sentiment.getScore())
                .sentimentLabel(sentiment.getLabel())
                .build();

        message = messageRepository.save(message);
        return toResponse(message);
    }

    // ── Get message by ID ───────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public MessageResponse getMessageById(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with ID: " + id));
        return toResponse(message);
    }

    // ── Get all messages in a chat room (ordered, paginated) ────────────────────
    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessagesByChatRoom(Long chatRoomId, Pageable pageable) {
        return messageRepository.findByChatRoomIdOrderBySentAtDesc(chatRoomId, pageable)
                .map(this::toResponse);
    }

    // ── Get all messages in a chat room (ordered list) ──────────────────────────
    @Transactional(readOnly = true)
    public List<MessageResponse> getChatRoomMessages(Long chatRoomId) {
        return messageRepository.findByChatRoomIdOrderBySentAtAsc(chatRoomId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Get private conversation between two users ──────────────────────────────
    @Transactional(readOnly = true)
    public List<MessageResponse> getConversation(Long userId1, Long userId2) {
        return messageRepository.findConversation(userId1, userId2)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Edit a message ──────────────────────────────────────────────────────────
    public MessageResponse editMessage(Long messageId, String newContent) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with ID: " + messageId));

        message.setContent(newContent);
        message.setIsEdited(true);
        message.setEditedAt(LocalDateTime.now());

        return toResponse(messageRepository.save(message));
    }

    // ── Soft delete a message ───────────────────────────────────────────────────
    public MessageResponse deleteMessage(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with ID: " + messageId));

        message.setIsDeleted(true);
        message.setDeletedAt(LocalDateTime.now());
        message.setContent("[Message supprimé]");

        return toResponse(messageRepository.save(message));
    }

    // ── Mark messages as read ───────────────────────────────────────────────────
    public void markConversationAsRead(Long receiverId, Long senderId) {
        messageRepository.markAsRead(receiverId, senderId);
    }

    // ── Count unread messages ───────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public long countUnreadMessages(Long receiverId) {
        return messageRepository.countByReceiverIdAndIsRead(receiverId, false);
    }

    // ── Get unread messages ─────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<MessageResponse> getUnreadMessages(Long receiverId) {
        return messageRepository.findByReceiverIdAndIsRead(receiverId, false)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── React to message ────────────────────────────────────────────────────────
    public MessageResponse reactToMessage(Long messageId, Long userId, String emoji) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with ID: " + messageId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Optional<MessageReaction> existing = messageReactionRepository.findByMessageIdAndUserId(messageId, userId);
        if (existing.isPresent()) {
            MessageReaction reaction = existing.get();
            if (reaction.getEmoji().equals(emoji)) {
                // Toggle off
                messageReactionRepository.delete(reaction);
                message.getReactions().remove(reaction);
            } else {
                // Change emoji
                reaction.setEmoji(emoji);
                messageReactionRepository.save(reaction);
            }
        } else {
            // New reaction
            MessageReaction reaction = MessageReaction.builder()
                    .message(message)
                    .user(user)
                    .emoji(emoji)
                    .build();
            reaction = messageReactionRepository.save(reaction);
            message.getReactions().add(reaction);
        }

        return toResponse(message);
    }

    // ── Get flagged messages (negative sentiment) for admin moderation ────────────
    @Transactional(readOnly = true)
    public List<MessageResponse> getFlaggedMessages() {
        return messageRepository.findBySentimentLabelOrderBySentAtDesc("negative")
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Get room sentiment statistics ────────────────────────────────────────────
    @Transactional(readOnly = true)
    public RoomSentimentStats getRoomSentimentStats(Long chatRoomId) {
        List<Message> messages = messageRepository.findByChatRoomIdOrderBySentAtAsc(chatRoomId);

        int totalMessages = 0;
        int analyzedMessages = 0;
        double totalScore = 0.0;
        int positiveCount = 0;
        int neutralCount = 0;
        int negativeCount = 0;

        for (Message msg : messages) {
            if (msg.getSentimentLabel() != null) {
                analyzedMessages++;
                totalScore += msg.getSentimentScore() != null ? msg.getSentimentScore() : 0;

                String label = msg.getSentimentLabel().toLowerCase();
                if (label.contains("positive")) {
                    positiveCount++;
                } else if (label.contains("negative")) {
                    negativeCount++;
                } else {
                    neutralCount++;
                }
            }
            totalMessages++;
        }

        double averageScore = analyzedMessages > 0 ? totalScore / analyzedMessages : 0.0;
        String overallLabel = analyzedMessages > 0 ? getLabelFromAverage(averageScore) : "neutral";

        return RoomSentimentStats.builder()
                .totalMessages(totalMessages)
                .analyzedMessages(analyzedMessages)
                .averageScore(Math.round(averageScore * 100.0) / 100.0)
                .overallLabel(overallLabel)
                .positiveCount(positiveCount)
                .neutralCount(neutralCount)
                .negativeCount(negativeCount)
                .build();
    }

    private String getLabelFromAverage(double score) {
        if (score <= -0.5) return "negative";
        if (score >= 0.5) return "positive";
        return "neutral";
    }

    // ── Mapper ──────────────────────────────────────────────────────────────────
    private MessageResponse toResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .mediaUrl(message.getMediaUrl())
                .thumbnailUrl(message.getThumbnailUrl())
                .fileType(message.getFileType())
                .fileName(message.getFileName())
                .fileSize(message.getFileSize())
                .isRead(message.getIsRead())
                .readAt(message.getReadAt())
                .isEdited(message.getIsEdited())
                .editedAt(message.getEditedAt())
                .isDeleted(message.getIsDeleted())
                .sentAt(message.getSentAt())
                .createdAt(message.getCreatedAt())
                .senderId(message.getSender() != null ? message.getSender().getId() : null)
                .senderName(message.getSender() != null ? message.getSender().getName() : null)
                .receiverId(message.getReceiver() != null ? message.getReceiver().getId() : null)
                .receiverName(message.getReceiver() != null ? message.getReceiver().getName() : null)
                .chatRoomId(message.getChatRoom() != null ? message.getChatRoom().getId() : null)
                .replyToId(message.getReplyTo() != null ? message.getReplyTo().getId() : null)
                .reactions(message.getReactions() != null
                        ? message.getReactions().stream()
                        .map(r -> tn.esprit.projetintegre.dto.response.MessageReactionResponse.builder()
                                .id(r.getId())
                                .emoji(r.getEmoji())
                                .userId(r.getUser() != null ? r.getUser().getId() : null)
                                .userName(r.getUser() != null ? r.getUser().getName() : null)
                                .createdAt(r.getCreatedAt())
                                .build())
                        .collect(Collectors.toList())
                        : null)
                .sentimentScore(message.getSentimentScore())
                .sentimentLabel(message.getSentimentLabel())
                .build();
    }
}