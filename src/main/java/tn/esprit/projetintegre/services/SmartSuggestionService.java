package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.projetintegre.dto.response.ChatRoomSuggestions;
import tn.esprit.projetintegre.dto.response.RoomSentimentStats;
import tn.esprit.projetintegre.dto.response.SmartSuggestion;
import tn.esprit.projetintegre.entities.ChatRoom;
import tn.esprit.projetintegre.entities.Message;
import tn.esprit.projetintegre.enums.SuggestionType;
import tn.esprit.projetintegre.repositories.ChatRoomRepository;
import tn.esprit.projetintegre.repositories.MessageRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ML-driven suggestion service that analyzes chat sentiment and generates
 * actionable recommendations for room owners.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmartSuggestionService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageService messageService;

    // Thresholds for detection
    private static final double VERY_NEGATIVE_THRESHOLD = -0.7;
    private static final double NEGATIVE_THRESHOLD = -0.3;
    private static final double POSITIVE_THRESHOLD = 0.5;
    private static final double VERY_POSITIVE_THRESHOLD = 0.7;
    
    private static final double TOXICITY_PERCENTAGE_HIGH = 0.30; // 30%
    private static final double TOXICITY_PERCENTAGE_CRITICAL = 0.50; // 50%
    private static final int MIN_MESSAGES_FOR_ANALYSIS = 10;
    private static final int TOXIC_MESSAGE_THRESHOLD = 5;
    
    /**
     * Generate ML-driven suggestions for a chat room based on sentiment analysis.
     */
    public ChatRoomSuggestions generateSuggestions(Long chatRoomId) {
        log.info("Generating smart suggestions for chat room {}", chatRoomId);
        
        try {
            if (chatRoomId == null) {
                throw new IllegalArgumentException("chatRoomId cannot be null");
            }
            
            ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                    .orElseThrow(() -> new RuntimeException("Chat room not found: " + chatRoomId));
            log.debug("Found chat room: {}", chatRoom.getName());
            
            RoomSentimentStats stats = messageService.getRoomSentimentStats(chatRoomId);
            if (stats == null) {
                throw new RuntimeException("Failed to get sentiment stats for room " + chatRoomId);
            }
            log.debug("Stats for room {}: total={}, analyzed={}, avgScore={}", 
                    chatRoomId, stats.getTotalMessages(), stats.getAnalyzedMessages(), stats.getAverageScore());
            
            List<Message> recentMessages = getRecentMessages(chatRoomId, 24); // Last 24 hours
            if (recentMessages == null) {
                recentMessages = new ArrayList<>();
            }
            
            List<Message> allMessages = messageRepository.findByChatRoomIdOrderBySentAtAsc(chatRoomId);
            if (allMessages == null) {
                allMessages = new ArrayList<>();
            }
            log.debug("Messages for room {}: recent={}, all={}", chatRoomId, recentMessages.size(), allMessages.size());
            
            List<SmartSuggestion> allSuggestions = new ArrayList<>();
        
        // Core sentiment-based detections
        try { allSuggestions.addAll(detectSentimentCrisis(stats, chatRoom)); } 
        catch (Exception e) { log.error("Error in detectSentimentCrisis: {}", e.getMessage()); }
        
        try { allSuggestions.addAll(detectSentimentDeclining(stats, recentMessages, allMessages)); }
        catch (Exception e) { log.error("Error in detectSentimentDeclining: {}", e.getMessage()); }
        
        try { allSuggestions.addAll(detectHighToxicity(stats, chatRoom)); }
        catch (Exception e) { log.error("Error in detectHighToxicity: {}", e.getMessage()); }
        
        try { allSuggestions.addAll(detectModerationNeeds(stats, chatRoom)); }
        catch (Exception e) { log.error("Error in detectModerationNeeds: {}", e.getMessage()); }
        
        // User behavior detections
        try { allSuggestions.addAll(detectToxicUsers(recentMessages, chatRoomId)); }
        catch (Exception e) { log.error("Error in detectToxicUsers: {}", e.getMessage()); }
        
        try { allSuggestions.addAll(detectPositiveContributors(recentMessages, chatRoomId)); }
        catch (Exception e) { log.error("Error in detectPositiveContributors: {}", e.getMessage()); }
        
        try { allSuggestions.addAll(detectDisruptiveMembers(recentMessages, chatRoomId)); }
        catch (Exception e) { log.error("Error in detectDisruptiveMembers: {}", e.getMessage()); }
        
        // Business/engagement detections
        try { allSuggestions.addAll(detectSponsorshipOpportunity(stats, chatRoom)); }
        catch (Exception e) { log.error("Error in detectSponsorshipOpportunity: {}", e.getMessage()); }
        
        try { allSuggestions.addAll(detectEventOpportunity(stats, chatRoom)); }
        catch (Exception e) { log.error("Error in detectEventOpportunity: {}", e.getMessage()); }
        
        try { allSuggestions.addAll(detectPremiumTierOpportunity(stats, chatRoom)); }
        catch (Exception e) { log.error("Error in detectPremiumTierOpportunity: {}", e.getMessage()); }
        
        // Content management detections
        try { allSuggestions.addAll(detectNegativeThreads(recentMessages, chatRoom)); }
        catch (Exception e) { log.error("Error in detectNegativeThreads: {}", e.getMessage()); }
        
        try { allSuggestions.addAll(detectNeedForPositiveContent(stats, chatRoom)); }
        catch (Exception e) { log.error("Error in detectNeedForPositiveContent: {}", e.getMessage()); }
        
        try { allSuggestions.addAll(detectGuidelinesNeeded(stats, chatRoom)); }
        catch (Exception e) { log.error("Error in detectGuidelinesNeeded: {}", e.getMessage()); }
        
        // Activity-based detections
        try { allSuggestions.addAll(detectLowActivity(stats, recentMessages, chatRoom)); }
        catch (Exception e) { log.error("Error in detectLowActivity: {}", e.getMessage()); }
        
        try { allSuggestions.addAll(detectArchiveOpportunity(allMessages, chatRoom)); }
        catch (Exception e) { log.error("Error in detectArchiveOpportunity: {}", e.getMessage()); }
        
        try { allSuggestions.addAll(detectReactivationNeeded(stats, recentMessages, chatRoom)); }
        catch (Exception e) { log.error("Error in detectReactivationNeeded: {}", e.getMessage()); }
        
        // Community health detections
        try { allSuggestions.addAll(detectCelebrateMilestone(stats, chatRoom)); }
        catch (Exception e) { log.error("Error in detectCelebrateMilestone: {}", e.getMessage()); }
        
        try { allSuggestions.addAll(detectCommunityGrowthOpportunity(stats, chatRoom)); }
        catch (Exception e) { log.error("Error in detectCommunityGrowthOpportunity: {}", e.getMessage()); }
        
        // Sort and categorize
        List<SmartSuggestion> highPriority = allSuggestions.stream()
                .filter(s -> "HIGH".equals(s.getPriority()) || Boolean.TRUE.equals(s.getIsUrgent()))
                .sorted(Comparator.comparing(SmartSuggestion::getConfidenceScore).reversed())
                .collect(Collectors.toList());
        
        List<SmartSuggestion> mediumPriority = allSuggestions.stream()
                .filter(s -> "MEDIUM".equals(s.getPriority()))
                .sorted(Comparator.comparing(SmartSuggestion::getConfidenceScore).reversed())
                .collect(Collectors.toList());
        
        List<SmartSuggestion> lowPriority = allSuggestions.stream()
                .filter(s -> "LOW".equals(s.getPriority()))
                .sorted(Comparator.comparing(SmartSuggestion::getConfidenceScore).reversed())
                .collect(Collectors.toList());
        
        // Calculate overall health
        double healthScore = calculateHealthScore(stats, allSuggestions);
        String healthStatus = determineHealthStatus(healthScore, allSuggestions);
        String healthTrend = determineTrend(stats, allMessages);
        
        return ChatRoomSuggestions.builder()
                .chatRoomId(chatRoomId)
                .chatRoomName(chatRoom.getName())
                .generatedAt(LocalDateTime.now())
                .overallHealthScore(healthScore)
                .healthStatus(healthStatus)
                .healthTrend(healthTrend)
                .highPriority(highPriority)
                .mediumPriority(mediumPriority)
                .lowPriority(lowPriority)
                .totalSuggestions(allSuggestions.size())
                .urgentCount((int) allSuggestions.stream().filter(s -> Boolean.TRUE.equals(s.getIsUrgent())).count())
                .memberActionsCount((int) allSuggestions.stream()
                        .filter(s -> Arrays.asList(
                                SuggestionType.BAN_MEMBER, SuggestionType.MUTE_MEMBER, 
                                SuggestionType.WARN_MEMBER, SuggestionType.PROMOTE_MEMBER)
                                .contains(s.getType())).count())
                .moderationActionsCount((int) allSuggestions.stream()
                        .filter(s -> Arrays.asList(
                                SuggestionType.ADD_MODERATOR, SuggestionType.REMOVE_MODERATOR,
                                SuggestionType.DELETE_NEGATIVE_THREAD)
                                .contains(s.getType())).count())
                .businessOpportunitiesCount((int) allSuggestions.stream()
                        .filter(s -> Arrays.asList(
                                SuggestionType.ADD_SPONSOR, SuggestionType.CREATE_PREMIUM_TIER,
                                SuggestionType.HOST_EVENT)
                                .contains(s.getType())).count())
                .keyInsights(generateKeyInsights(stats, allSuggestions))
                .recommendedNextAction(highPriority.isEmpty() ? 
                        (mediumPriority.isEmpty() ? "No immediate action needed" : mediumPriority.get(0).getTitle())
                        : highPriority.get(0).getTitle())
                .riskLevel(calculateRiskLevel(allSuggestions, stats))
                .riskFactors(extractRiskFactors(allSuggestions))
                .build();
        } catch (Exception e) {
            log.error("Error generating suggestions for room {}: {}", chatRoomId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate suggestions: " + e.getMessage(), e);
        }
    }
    
    // ==================== SENTIMENT CRISIS DETECTION ====================
    
    private List<SmartSuggestion> detectSentimentCrisis(RoomSentimentStats stats, ChatRoom chatRoom) {
        List<SmartSuggestion> suggestions = new ArrayList<>();
        
        if (stats.getAnalyzedMessages() < MIN_MESSAGES_FOR_ANALYSIS) {
            return suggestions;
        }
        
        double negativeRatio = (double) stats.getNegativeCount() / stats.getAnalyzedMessages();
        
        // Critical: >50% negative messages
        if (negativeRatio >= TOXICITY_PERCENTAGE_CRITICAL || stats.getAverageScore() <= VERY_NEGATIVE_THRESHOLD) {
            suggestions.add(SmartSuggestion.builder()
                    .type(SuggestionType.SENTIMENT_CRISIS)
                    .title("🚨 URGENT: Chat Sentiment Crisis Detected")
                    .description(String.format(
                            "%.0f%% of recent messages are negative (%.2f sentiment score). " +
                            "Immediate intervention recommended to prevent community damage.",
                            negativeRatio * 100, stats.getAverageScore()))
                    .priority("HIGH")
                    .confidenceScore(0.95)
                    .severityScore(0.9)
                    .triggeredBy(String.format("Negative ratio: %.2f, Score: %.2f", negativeRatio, stats.getAverageScore()))
                    .sentimentScore(stats.getAverageScore())
                    .affectedMessageCount(stats.getNegativeCount())
                    .detectedAt(LocalDateTime.now())
                    .isUrgent(true)
                    .recommendedActions(Arrays.asList(
                            "Review and delete toxic messages",
                            "Issue community warning",
                            "Consider temporary chat freeze",
                            "Contact top contributors for support"
                    ))
                    .trendDirection("DECLINING")
                    .totalMessages(stats.getTotalMessages())
                    .positiveCount(stats.getPositiveCount())
                    .negativeCount(stats.getNegativeCount())
                    .neutralCount(stats.getNeutralCount())
                    .build());
            
            // Also suggest closing if extremely bad
            if (negativeRatio >= 0.70) {
                suggestions.add(SmartSuggestion.builder()
                        .type(SuggestionType.CLOSE_CHAT)
                        .title("⚠️ Consider Closing This Chat")
                        .description("Extreme toxicity detected (70%+ negative). This chat may be beyond saving.")
                        .priority("HIGH")
                        .confidenceScore(0.85)
                        .severityScore(1.0)
                        .triggeredBy("Extreme negativity: " + (negativeRatio * 100) + "%")
                        .isUrgent(true)
                        .build());
            }
        }
        
        return suggestions;
    }
    
    // ==================== SENTIMENT DECLINING DETECTION ====================
    
    private List<SmartSuggestion> detectSentimentDeclining(
            RoomSentimentStats stats, 
            List<Message> recentMessages,
            List<Message> allMessages) {
        
        List<SmartSuggestion> suggestions = new ArrayList<>();
        
        if (allMessages.size() < MIN_MESSAGES_FOR_ANALYSIS * 2) {
            return suggestions;
        }
        
        // Split into two halves
        int mid = allMessages.size() / 2;
        List<Message> olderMessages = allMessages.subList(0, mid);
        List<Message> newerMessages = allMessages.subList(mid, allMessages.size());
        
        double olderScore = calculateAverageScore(olderMessages);
        double newerScore = calculateAverageScore(newerMessages);
        
        double decline = olderScore - newerScore;
        double declinePercent = olderScore != 0 ? (decline / Math.abs(olderScore)) * 100 : 0;
        
        // Significant decline detected (>20% drop or >0.3 absolute drop)
        if (decline > 0.3 || (olderScore > 0 && declinePercent > 20)) {
            String severity = decline > 0.5 ? "HIGH" : "MEDIUM";
            double confidence = Math.min(0.95, 0.7 + (decline * 0.3));
            
            suggestions.add(SmartSuggestion.builder()
                    .type(SuggestionType.SENTIMENT_DECLINING)
                    .title("📉 Sentiment Trend: Declining")
                    .description(String.format(
                            "Chat sentiment has declined by %.1f%% (from %.2f to %.2f). " +
                            "Take action to reverse this trend before it becomes critical.",
                            Math.abs(declinePercent), olderScore, newerScore))
                    .priority(severity)
                    .confidenceScore(confidence)
                    .severityScore(Math.min(1.0, decline * 0.8))
                    .triggeredBy(String.format("Score drop: %.2f (%.1f%%)", decline, declinePercent))
                    .sentimentScore(newerScore)
                    .detectedAt(LocalDateTime.now())
                    .trendDirection("DECLINING")
                    .trendChangePercent(declinePercent)
                    .recommendedActions(Arrays.asList(
                            "Post positive discussion topic",
                            "Acknowledge concerns if any",
                            "Highlight positive community rules",
                            "Engage with neutral members"
                    ))
                    .build());
        }
        
        return suggestions;
    }
    
    // ==================== HIGH TOXICITY DETECTION ====================
    
    private List<SmartSuggestion> detectHighToxicity(RoomSentimentStats stats, ChatRoom chatRoom) {
        List<SmartSuggestion> suggestions = new ArrayList<>();
        
        if (stats.getAnalyzedMessages() < MIN_MESSAGES_FOR_ANALYSIS) {
            return suggestions;
        }
        
        double negativeRatio = (double) stats.getNegativeCount() / stats.getAnalyzedMessages();
        
        // High toxicity (30-50% negative)
        if (negativeRatio >= TOXICITY_PERCENTAGE_HIGH && negativeRatio < TOXICITY_PERCENTAGE_CRITICAL) {
            suggestions.add(SmartSuggestion.builder()
                    .type(SuggestionType.HIGH_TOXICITY_DETECTED)
                    .title("⚠️ High Toxicity Alert")
                    .description(String.format(
                            "%.0f%% of messages show negative sentiment. " +
                            "Monitor closely and consider proactive moderation.",
                            negativeRatio * 100))
                    .priority("MEDIUM")
                    .confidenceScore(0.8)
                    .severityScore(0.6)
                    .triggeredBy(String.format("Negative ratio: %.2f", negativeRatio))
                    .sentimentScore(stats.getAverageScore())
                    .affectedMessageCount(stats.getNegativeCount())
                    .detectedAt(LocalDateTime.now())
                    .recommendedActions(Arrays.asList(
                            "Review flagged messages",
                            "Check for repeat offenders",
                            "Post reminder about community guidelines"
                    ))
                    .build());
        }
        
        return suggestions;
    }
    
    // ==================== MODERATION NEEDS ====================
    
    private List<SmartSuggestion> detectModerationNeeds(RoomSentimentStats stats, ChatRoom chatRoom) {
        List<SmartSuggestion> suggestions = new ArrayList<>();
        
        // High volume of negative messages suggests need for more moderation
        if (stats.getNegativeCount() > 20 && stats.getNegativeCount() > stats.getPositiveCount()) {
            suggestions.add(SmartSuggestion.builder()
                    .type(SuggestionType.ADD_MODERATOR)
                    .title("👥 Add More Moderators")
                    .description(String.format(
                            "High negative message volume (%d) vs positive (%d). " +
                            "Additional moderation support recommended.",
                            stats.getNegativeCount(), stats.getPositiveCount()))
                    .priority("MEDIUM")
                    .confidenceScore(0.75)
                    .severityScore(0.5)
                    .triggeredBy("Negative:Positive ratio > 1:1")
                    .recommendedActions(Arrays.asList(
                            "Invite trusted members as moderators",
                            "Establish moderation schedule",
                            "Create moderator guidelines"
                    ))
                    .build());
        }
        
        return suggestions;
    }
    
    // ==================== TOXIC USER DETECTION ====================
    
    private List<SmartSuggestion> detectToxicUsers(List<Message> messages, Long chatRoomId) {
        List<SmartSuggestion> suggestions = new ArrayList<>();
        
        // Group messages by sender
        Map<Long, List<Message>> messagesByUser = messages.stream()
                .filter(m -> m.getSender() != null)
                .collect(Collectors.groupingBy(m -> m.getSender().getId()));
        
        for (Map.Entry<Long, List<Message>> entry : messagesByUser.entrySet()) {
            Long userId = entry.getKey();
            List<Message> userMessages = entry.getValue();
            
            if (userMessages.size() < 3) continue; // Need minimum data
            
            long negativeCount = userMessages.stream()
                    .filter(m -> m.getSentimentLabel() != null && 
                            m.getSentimentLabel().toLowerCase().contains("negative"))
                    .count();
            
            double negativeRatio = (double) negativeCount / userMessages.size();
            double avgScore = userMessages.stream()
                    .filter(m -> m.getSentimentScore() != null)
                    .mapToDouble(Message::getSentimentScore)
                    .average().orElse(0.0);
            
            // User with >70% negative messages and at least 5 messages
            if (negativeRatio >= 0.70 && negativeCount >= TOXIC_MESSAGE_THRESHOLD) {
                Message firstMessage = userMessages.get(0);
                String userName = firstMessage.getSender() != null ? firstMessage.getSender().getName() : "Unknown";
                if (userName == null) userName = "User " + userId;
                
                // Determine action based on severity
                SuggestionType actionType = negativeRatio >= 0.90 ? SuggestionType.BAN_MEMBER :
                        negativeRatio >= 0.80 ? SuggestionType.MUTE_MEMBER : SuggestionType.WARN_MEMBER;
                
                String actionTitle = actionType == SuggestionType.BAN_MEMBER ? "🚫 Consider Banning Member" :
                        actionType == SuggestionType.MUTE_MEMBER ? "🔇 Consider Muting Member" :
                                "⚠️ Warn Toxic Member";
                
                suggestions.add(SmartSuggestion.builder()
                        .type(actionType)
                        .title(actionTitle + ": " + userName)
                        .description(String.format(
                                "User %s has %.0f%% negative messages (%d of %d). " +
                                "Average sentiment: %.2f. Consider %s action.",
                                userName, negativeRatio * 100, negativeCount, userMessages.size(),
                                avgScore, actionType.getDisplayName().toLowerCase()))
                        .priority(negativeRatio >= 0.90 ? "HIGH" : "MEDIUM")
                        .confidenceScore(Math.min(0.95, 0.7 + negativeRatio * 0.25))
                        .severityScore(negativeRatio)
                        .triggeredBy(String.format("User negative ratio: %.2f", negativeRatio))
                        .sentimentScore(avgScore)
                        .affectedMessageCount((int) negativeCount)
                        .detectedAt(LocalDateTime.now())
                        .targetUserId(userId)
                        .targetUserName(userName)
                        .targetUserNegativeCount((int) negativeCount)
                        .targetUserSentimentScore(avgScore)
                        .recommendedActions(Arrays.asList(
                                "Review user's message history",
                                "Check if first-time or repeat offender",
                                "Consider warning before severe action",
                                "Document decision for transparency"
                        ))
                        .build());
            }
        }
        
        return suggestions;
    }
    
    // ==================== POSITIVE CONTRIBUTOR DETECTION ====================
    
    private List<SmartSuggestion> detectPositiveContributors(List<Message> messages, Long chatRoomId) {
        List<SmartSuggestion> suggestions = new ArrayList<>();
        
        Map<Long, List<Message>> messagesByUser = messages.stream()
                .filter(m -> m.getSender() != null)
                .collect(Collectors.groupingBy(m -> m.getSender().getId()));
        
        for (Map.Entry<Long, List<Message>> entry : messagesByUser.entrySet()) {
            Long userId = entry.getKey();
            List<Message> userMessages = entry.getValue();
            
            if (userMessages.size() < 5) continue;
            
            long positiveCount = userMessages.stream()
                    .filter(m -> m.getSentimentLabel() != null && 
                            m.getSentimentLabel().toLowerCase().contains("positive"))
                    .count();
            
            double positiveRatio = (double) positiveCount / userMessages.size();
            double avgScore = userMessages.stream()
                    .filter(m -> m.getSentimentScore() != null)
                    .mapToDouble(Message::getSentimentScore)
                    .average().orElse(0.0);
            
            // Very positive contributor (>80% positive, score > 0.6)
            if (positiveRatio >= 0.80 && avgScore >= POSITIVE_THRESHOLD) {
                Message firstMessage = userMessages.get(0);
                String userName = firstMessage.getSender() != null ? firstMessage.getSender().getName() : "Unknown";
                if (userName == null) userName = "User " + userId;
                
                suggestions.add(SmartSuggestion.builder()
                        .type(SuggestionType.PROMOTE_MEMBER)
                        .title("⭐ Promote Positive Contributor: " + userName)
                        .description(String.format(
                                "User %s is a valuable community member with %.0f%% positive messages " +
                                "and %.2f average sentiment. Consider promoting to moderator or highlighting their contributions.",
                                userName, positiveRatio * 100, avgScore))
                        .priority("LOW")
                        .confidenceScore(0.8)
                        .severityScore(0.0)
                        .triggeredBy(String.format("User positive ratio: %.2f", positiveRatio))
                        .sentimentScore(avgScore)
                        .targetUserId(userId)
                        .targetUserName(userName)
                        .targetUserNegativeCount((int) positiveCount)
                        .recommendedActions(Arrays.asList(
                                "Consider moderator promotion",
                                "Feature in community spotlight",
                                "Thank them publicly",
                                "Give special recognition"
                        ))
                        .trendDirection("IMPROVING")
                        .build());
            }
        }
        
        return suggestions;
    }
    
    // ==================== DISRUPTIVE MEMBER DETECTION ====================
    
    private List<SmartSuggestion> detectDisruptiveMembers(List<Message> messages, Long chatRoomId) {
        List<SmartSuggestion> suggestions = new ArrayList<>();
        
        // Detect users with high message volume but very negative sentiment
        Map<Long, List<Message>> messagesByUser = messages.stream()
                .filter(m -> m.getSender() != null)
                .collect(Collectors.groupingBy(m -> m.getSender().getId()));
        
        for (Map.Entry<Long, List<Message>> entry : messagesByUser.entrySet()) {
            Long userId = entry.getKey();
            List<Message> userMessages = entry.getValue();
            
            if (userMessages.size() < 10) continue;
            
            double avgScore = userMessages.stream()
                    .filter(m -> m.getSentimentScore() != null)
                    .mapToDouble(Message::getSentimentScore)
                    .average().orElse(0.0);
            
            // High volume + negative sentiment = disruptive
            if (userMessages.size() > 15 && avgScore < NEGATIVE_THRESHOLD) {
                Message firstMessage = userMessages.get(0);
                String userName = firstMessage.getSender() != null ? firstMessage.getSender().getName() : "Unknown";
                if (userName == null) userName = "User " + userId;
                
                suggestions.add(SmartSuggestion.builder()
                        .type(SuggestionType.WARN_MEMBER)
                        .title("⚠️ Disruptive Activity: " + userName)
                        .description(String.format(
                                "User %s sent %d messages with %.2f avg sentiment. " +
                                "High activity with negative tone may be disrupting conversations.",
                                userName, userMessages.size(), avgScore))
                        .priority("MEDIUM")
                        .confidenceScore(0.75)
                        .severityScore(0.6)
                        .targetUserId(userId)
                        .targetUserName(userName)
                        .recommendedActions(Arrays.asList(
                                "Review conversation patterns",
                                "Private message to discuss behavior",
                                "Set message rate limits if needed"
                        ))
                        .build());
            }
        }
        
        return suggestions;
    }
    
    // ==================== SPONSORSHIP OPPORTUNITY ====================
    
    private List<SmartSuggestion> detectSponsorshipOpportunity(RoomSentimentStats stats, ChatRoom chatRoom) {
        List<SmartSuggestion> suggestions = new ArrayList<>();
        
        if (stats.getAnalyzedMessages() < MIN_MESSAGES_FOR_ANALYSIS) {
            return suggestions;
        }
        
        double positiveRatio = (double) stats.getPositiveCount() / stats.getAnalyzedMessages();
        
        // Excellent sentiment for sponsorship
        if (positiveRatio >= 0.70 && stats.getAverageScore() >= VERY_POSITIVE_THRESHOLD) {
            suggestions.add(SmartSuggestion.builder()
                    .type(SuggestionType.ADD_SPONSOR)
                    .title("💰 Sponsorship Opportunity")
                    .description(String.format(
                            "Excellent community health! %.0f%% positive sentiment (score: %.2f). " +
                            "Perfect time to introduce sponsorship opportunities.",
                            positiveRatio * 100, stats.getAverageScore()))
                    .priority("LOW")
                    .confidenceScore(0.85)
                    .severityScore(0.0)
                    .triggeredBy(String.format("Positive ratio: %.2f, Score: %.2f", positiveRatio, stats.getAverageScore()))
                    .sentimentScore(stats.getAverageScore())
                    .recommendedActions(Arrays.asList(
                            "Reach out to potential sponsors",
                            "Prepare sponsorship packages",
                            "Announce community milestone",
                            "Show engagement metrics to sponsors"
                    ))
                    .trendDirection("IMPROVING")
                    .build());
        }
        
        return suggestions;
    }
    
    // ==================== EVENT OPPORTUNITY ====================
    
    private List<SmartSuggestion> detectEventOpportunity(RoomSentimentStats stats, ChatRoom chatRoom) {
        List<SmartSuggestion> suggestions = new ArrayList<>();
        
        if (stats.getAnalyzedMessages() < MIN_MESSAGES_FOR_ANALYSIS) {
            return suggestions;
        }
        
        double positiveRatio = (double) stats.getPositiveCount() / stats.getAnalyzedMessages();
        
        // Good vibes for an event
        if (positiveRatio >= 0.60 && stats.getTotalMessages() > 50) {
            suggestions.add(SmartSuggestion.builder()
                    .type(SuggestionType.HOST_EVENT)
                    .title("🎉 Perfect Time for an Event!")
                    .description(String.format(
                            "Community is active and positive (%.0f%% positive, %d messages). " +
                            "Consider hosting a virtual event or AMA to capitalize on the good energy!",
                            positiveRatio * 100, stats.getTotalMessages()))
                    .priority("LOW")
                    .confidenceScore(0.8)
                    .severityScore(0.0)
                    .recommendedActions(Arrays.asList(
                            "Schedule community AMA",
                            "Plan a virtual meetup",
                            "Host a contest or giveaway",
                            "Celebrate community achievements"
                    ))
                    .trendDirection("IMPROVING")
                    .build());
        }
        
        return suggestions;
    }
    
    // ==================== PREMIUM TIER OPPORTUNITY ====================
    
    private List<SmartSuggestion> detectPremiumTierOpportunity(RoomSentimentStats stats, ChatRoom chatRoom) {
        List<SmartSuggestion> suggestions = new ArrayList<>();
        
        // High engagement with good sentiment suggests premium opportunity
        if (stats.getTotalMessages() > 200 && stats.getAverageScore() > 0.3) {
            suggestions.add(SmartSuggestion.builder()
                    .type(SuggestionType.CREATE_PREMIUM_TIER)
                    .title("👑 Consider Premium Tier")
                    .description(String.format(
                            "High engagement (%d messages) with good sentiment (%.2f). " +
                            "Community may be ready for premium features or subscriptions.",
                            stats.getTotalMessages(), stats.getAverageScore()))
                    .priority("LOW")
                            .confidenceScore(0.7)
                            .severityScore(0.0)
                            .recommendedActions(Arrays.asList(
                                    "Survey members about premium interest",
                                    "Design exclusive benefits",
                                    "Plan tiered pricing structure"
                            ))
                            .build());
        }
        
        return suggestions;
    }
    
    // ==================== NEGATIVE THREAD DETECTION ====================
    
    private List<SmartSuggestion> detectNegativeThreads(List<Message> messages, ChatRoom chatRoom) {
        List<SmartSuggestion> suggestions = new ArrayList<>();
        
        // Look for consecutive negative messages (potential toxic thread)
        int consecutiveNegative = 0;
        int maxConsecutive = 0;
        Message threadStart = null;
        
        for (Message msg : messages) {
            if (msg.getSentimentLabel() != null && 
                    msg.getSentimentLabel().toLowerCase().contains("negative")) {
                consecutiveNegative++;
                if (consecutiveNegative == 1) {
                    threadStart = msg;
                }
                maxConsecutive = Math.max(maxConsecutive, consecutiveNegative);
            } else {
                consecutiveNegative = 0;
            }
        }
        
        // 5+ consecutive negative messages = toxic thread
        if (maxConsecutive >= 5) {
            suggestions.add(SmartSuggestion.builder()
                    .type(SuggestionType.DELETE_NEGATIVE_THREAD)
                    .title("🧹 Toxic Thread Detected")
                    .description(String.format(
                            "Found a thread with %d consecutive negative messages. " +
                            "Consider deleting or archiving this conversation.",
                            maxConsecutive))
                    .priority("MEDIUM")
                    .confidenceScore(0.8)
                    .severityScore(0.6)
                    .affectedMessageCount(maxConsecutive)
                    .recommendedActions(Arrays.asList(
                            "Review the thread content",
                            "Delete if excessively toxic",
                            "Warn participants if needed",
                            "Post redirecting positive message"
                    ))
                    .build());
        }
        
        return suggestions;
    }
    
    // ==================== NEED FOR POSITIVE CONTENT ====================
    
    private List<SmartSuggestion> detectNeedForPositiveContent(RoomSentimentStats stats, ChatRoom chatRoom) {
        List<SmartSuggestion> suggestions = new ArrayList<>();
        
        double neutralRatio = (double) stats.getNeutralCount() / Math.max(1, stats.getAnalyzedMessages());
        
        // Too much neutral/flat content
        if (neutralRatio > 0.70 && stats.getPositiveCount() < stats.getAnalyzedMessages() * 0.20) {
            suggestions.add(SmartSuggestion.builder()
                    .type(SuggestionType.START_POSITIVE_TOPIC)
                    .title("💡 Spark Some Positivity")
                    .description(String.format(
                            "%.0f%% of messages are neutral with low positive engagement. " +
                            "Start a fun discussion to energize the community!",
                            neutralRatio * 100))
                    .priority("LOW")
                    .confidenceScore(0.75)
                    .severityScore(0.2)
                    .recommendedActions(Arrays.asList(
                            "Post a fun question",
                            "Share inspiring news",
                            "Start a photo sharing thread",
                            "Announce a mini-contest"
                    ))
                    .build());
        }
        
        return suggestions;
    }
    
    // ==================== GUIDELINES NEEDED ====================
    
    private List<SmartSuggestion> detectGuidelinesNeeded(RoomSentimentStats stats, ChatRoom chatRoom) {
        List<SmartSuggestion> suggestions = new ArrayList<>();
        
        double negativeRatio = (double) stats.getNegativeCount() / Math.max(1, stats.getAnalyzedMessages());
        
        // Declining sentiment with moderate negativity
        if (negativeRatio >= 0.20 && negativeRatio < 0.30 && stats.getAverageScore() < 0) {
            suggestions.add(SmartSuggestion.builder()
                    .type(SuggestionType.POST_COMMUNITY_GUIDELINES)
                    .title("📋 Reminder: Community Guidelines")
                    .description(String.format(
                            "Sentiment is declining (%.2f score, %.0f%% negative). " +
                            "A friendly reminder about community guidelines may help.",
                            stats.getAverageScore(), negativeRatio * 100))
                    .priority("MEDIUM")
                    .confidenceScore(0.7)
                    .severityScore(0.4)
                    .sentimentScore(stats.getAverageScore())
                    .recommendedActions(Arrays.asList(
                            "Post community guidelines",
                            "Highlight positive behavior examples",
                            "Encourage constructive discussions"
                    ))
                    .build());
        }
        
        return suggestions;
    }
    
    // ==================== LOW ACTIVITY DETECTION ====================
    
    private List<SmartSuggestion> detectLowActivity(
            RoomSentimentStats stats, 
            List<Message> recentMessages,
            ChatRoom chatRoom) {
        
        List<SmartSuggestion> suggestions = new ArrayList<>();
        
        // Very low recent activity
        if (recentMessages.size() < 5 && stats.getTotalMessages() > 50) {
            suggestions.add(SmartSuggestion.builder()
                    .type(SuggestionType.REACTIVATE_CHAT)
                    .title("😴 Chat Needs Reactivation")
                    .description(String.format(
                            "Only %d messages in the last 24 hours. " +
                            "This chat was previously active and may need a boost.",
                            recentMessages.size()))
                    .priority("LOW")
                    .confidenceScore(0.7)
                    .severityScore(0.3)
                    .affectedMessageCount(recentMessages.size())
                    .recommendedActions(Arrays.asList(
                            "Post an engaging question",
                            "Share interesting content",
                            "Tag active members",
                            "Announce upcoming plans"
                    ))
                    .trendDirection("DECLINING")
                    .build());
        }
        
        // New chat with very low engagement
        if (stats.getTotalMessages() < 20 && recentMessages.isEmpty()) {
            suggestions.add(SmartSuggestion.builder()
                    .type(SuggestionType.INVITE_MORE_MEMBERS)
                    .title("👋 Invite More Members")
                    .description("This chat is new and needs more participants. " +
                            "Invite members to build critical mass.")
                    .priority("MEDIUM")
                    .confidenceScore(0.75)
                    .severityScore(0.4)
                    .recommendedActions(Arrays.asList(
                            "Share invite link",
                            "Invite from related communities",
                            "Promote on social media",
                            "Partner with similar groups"
                    ))
                    .build());
        }
        
        return suggestions;
    }
    
    // ==================== ARCHIVE OPPORTUNITY ====================
    
    private List<SmartSuggestion> detectArchiveOpportunity(List<Message> allMessages, ChatRoom chatRoom) {
        List<SmartSuggestion> suggestions = new ArrayList<>();
        
        if (allMessages.isEmpty()) {
            return suggestions;
        }
        
        // Check if chat has been inactive for a long time
        Message lastMessage = allMessages.get(allMessages.size() - 1);
        LocalDateTime lastActivity = lastMessage.getSentAt();
        
        if (lastActivity != null && lastActivity.isBefore(LocalDateTime.now().minusDays(30))) {
            suggestions.add(SmartSuggestion.builder()
                    .type(SuggestionType.ARCHIVE_CHAT)
                    .title("📦 Consider Archiving")
                    .description(String.format(
                            "No activity since %s (30+ days ago). " +
                            "Consider archiving this chat to keep the workspace clean.",
                            lastActivity.toLocalDate()))
                    .priority("LOW")
                    .confidenceScore(0.8)
                    .severityScore(0.1)
                    .recommendedActions(Arrays.asList(
                            "Notify members before archiving",
                            "Export important content",
                            "Create summary of key discussions"
                    ))
                    .build());
        }
        
        return suggestions;
    }
    
    // ==================== REACTIVATION NEEDED ====================
    
    private List<SmartSuggestion> detectReactivationNeeded(
            RoomSentimentStats stats, 
            List<Message> recentMessages,
            ChatRoom chatRoom) {
        
        List<SmartSuggestion> suggestions = new ArrayList<>();
        
        // Was active, now quiet
        if (stats.getTotalMessages() > 100 && recentMessages.size() < 3) {
            suggestions.add(SmartSuggestion.builder()
                    .type(SuggestionType.REACTIVATE_CHAT)
                    .title("🔥 Bring Back the Energy")
                    .description(String.format(
                            "This chat has %d total messages but only %d recent ones. " +
                            "Previous members may still be interested - reignite the conversation!",
                            stats.getTotalMessages(), recentMessages.size()))
                    .priority("MEDIUM")
                    .confidenceScore(0.75)
                    .severityScore(0.4)
                    .recommendedActions(Arrays.asList(
                            "Reach out to previous contributors",
                            "Share what's new",
                            "Ask for topic suggestions",
                            "Create a 'welcome back' message"
                    ))
                    .build());
        }
        
        return suggestions;
    }
    
    // ==================== CELEBRATE MILESTONE ====================
    
    private List<SmartSuggestion> detectCelebrateMilestone(RoomSentimentStats stats, ChatRoom chatRoom) {
        List<SmartSuggestion> suggestions = new ArrayList<>();
        
        // Milestone messages with excellent sentiment
        boolean isMilestone = stats.getTotalMessages() >= 100 && stats.getTotalMessages() % 100 < 10;
        
        if (isMilestone && stats.getAverageScore() > POSITIVE_THRESHOLD) {
            suggestions.add(SmartSuggestion.builder()
                    .type(SuggestionType.CELEBRATE_MILESTONE)
                    .title("🎊 Celebrate Your Community!")
                    .description(String.format(
                            "Amazing! %d messages with %.2f sentiment score. " +
                            "Your community is thriving - celebrate this milestone together!",
                            stats.getTotalMessages(), stats.getAverageScore()))
                    .priority("LOW")
                    .confidenceScore(0.9)
                    .severityScore(0.0)
                    .recommendedActions(Arrays.asList(
                            "Thank your community members",
                            "Share milestone announcement",
                            "Plan a celebration event",
                            "Highlight top contributors"
                    ))
                    .trendDirection("IMPROVING")
                    .build());
        }
        
        return suggestions;
    }
    
    // ==================== COMMUNITY GROWTH OPPORTUNITY ====================
    
    private List<SmartSuggestion> detectCommunityGrowthOpportunity(RoomSentimentStats stats, ChatRoom chatRoom) {
        List<SmartSuggestion> suggestions = new ArrayList<>();
        
        // Healthy chat that could benefit from more members
        if (stats.getTotalMessages() > 50 && stats.getAverageScore() > 0.4 && stats.getTotalMessages() < 200) {
            double engagementRate = (double) stats.getPositiveCount() / Math.max(1, stats.getTotalMessages());
            
            if (engagementRate > 0.5) {
                suggestions.add(SmartSuggestion.builder()
                        .type(SuggestionType.INVITE_MORE_MEMBERS)
                        .title("📈 Ready for Growth!")
                        .description(String.format(
                                "Strong engagement (%.0f%% positive, score: %.2f). " +
                                "This community is ready to grow - invite more members!",
                                engagementRate * 100, stats.getAverageScore()))
                        .priority("LOW")
                        .confidenceScore(0.8)
                        .severityScore(0.0)
                        .recommendedActions(Arrays.asList(
                                "Share invite links",
                                "Cross-promote in related communities",
                                "Encourage members to invite friends",
                                "Create referral rewards"
                        ))
                        .trendDirection("IMPROVING")
                        .build());
            }
        }
        
        return suggestions;
    }
    
    // ==================== HELPER METHODS ====================
    
    private List<Message> getRecentMessages(Long chatRoomId, int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        List<Message> allMessages = messageRepository.findByChatRoomIdOrderBySentAtAsc(chatRoomId);
        if (allMessages == null) {
            return new ArrayList<>();
        }
        return allMessages.stream()
                .filter(m -> m.getSentAt() != null && m.getSentAt().isAfter(cutoff))
                .collect(Collectors.toList());
    }
    
    private double calculateAverageScore(List<Message> messages) {
        return messages.stream()
                .filter(m -> m.getSentimentScore() != null)
                .mapToDouble(Message::getSentimentScore)
                .average()
                .orElse(0.0);
    }
    
    private double calculateHealthScore(RoomSentimentStats stats, List<SmartSuggestion> suggestions) {
        long urgentCount = suggestions.stream().filter(s -> Boolean.TRUE.equals(s.getIsUrgent())).count();
        long highPriorityCount = suggestions.stream().filter(s -> "HIGH".equals(s.getPriority())).count();
        
        // Base score from sentiment
        double baseScore = (stats.getAverageScore() + 1) / 2; // Normalize to 0-1
        
        // Penalties
        double penalty = (urgentCount * 0.15) + (highPriorityCount * 0.10);
        
        return Math.max(0, Math.min(1, baseScore - penalty));
    }
    
    private String determineHealthStatus(double healthScore, List<SmartSuggestion> suggestions) {
        long urgentCount = suggestions.stream().filter(s -> Boolean.TRUE.equals(s.getIsUrgent())).count();
        
        if (urgentCount > 0 || healthScore < 0.3) return "CRITICAL";
        if (healthScore < 0.6) return "WARNING";
        return "HEALTHY";
    }
    
    private String determineTrend(RoomSentimentStats stats, List<Message> allMessages) {
        if (allMessages.size() < 20) return "STABLE";
        
        int mid = allMessages.size() / 2;
        List<Message> older = allMessages.subList(0, mid);
        List<Message> newer = allMessages.subList(mid, allMessages.size());
        
        double olderScore = calculateAverageScore(older);
        double newerScore = calculateAverageScore(newer);
        
        double change = newerScore - olderScore;
        
        if (change > 0.15) return "IMPROVING";
        if (change < -0.15) return "DECLINING";
        return "STABLE";
    }
    
    private String calculateRiskLevel(List<SmartSuggestion> suggestions, RoomSentimentStats stats) {
        long urgentCount = suggestions.stream().filter(s -> Boolean.TRUE.equals(s.getIsUrgent())).count();
        long highCount = suggestions.stream().filter(s -> "HIGH".equals(s.getPriority())).count();
        
        if (urgentCount >= 2 || stats.getAverageScore() < VERY_NEGATIVE_THRESHOLD) return "CRITICAL";
        if (urgentCount == 1 || highCount >= 2 || stats.getAverageScore() < NEGATIVE_THRESHOLD) return "HIGH";
        if (highCount == 1 || (double) stats.getNegativeCount() / Math.max(1, stats.getAnalyzedMessages()) > 0.25) return "MEDIUM";
        if ((double) stats.getNegativeCount() / Math.max(1, stats.getAnalyzedMessages()) > 0.15) return "LOW";
        return "NONE";
    }
    
    private List<String> extractRiskFactors(List<SmartSuggestion> suggestions) {
        return suggestions.stream()
                .filter(s -> s.getSeverityScore() > 0.5)
                .map(SmartSuggestion::getTriggeredBy)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
    
    private List<String> generateKeyInsights(RoomSentimentStats stats, List<SmartSuggestion> suggestions) {
        List<String> insights = new ArrayList<>();
        
        // Sentiment insight
        if (stats.getAnalyzedMessages() > 0) {
            double posRatio = (double) stats.getPositiveCount() / stats.getAnalyzedMessages();
            double negRatio = (double) stats.getNegativeCount() / stats.getAnalyzedMessages();
            
            if (posRatio > negRatio * 2) {
                insights.add("Community is very positive - maintain current approach");
            } else if (negRatio > posRatio) {
                insights.add("Negative messages outweigh positive - intervention needed");
            } else {
                insights.add("Balanced sentiment - monitor for changes");
            }
        }
        
        // Activity insight
        if (stats.getTotalMessages() > 100) {
            insights.add("High message volume indicates active community");
        } else if (stats.getTotalMessages() < 20) {
            insights.add("Low activity - focus on growing membership");
        }
        
        // Suggestion-based insight
        long memberIssues = suggestions.stream()
                .filter(s -> Arrays.asList(SuggestionType.BAN_MEMBER, SuggestionType.MUTE_MEMBER, 
                        SuggestionType.WARN_MEMBER).contains(s.getType()))
                .count();
        
        if (memberIssues > 2) {
            insights.add("Multiple member behavior issues - consider stricter moderation");
        }
        
        if (memberIssues == 0 && stats.getAverageScore() > 0.5) {
            insights.add("Member behavior is generally good - community is well-managed");
        }
        
        return insights;
    }
}
