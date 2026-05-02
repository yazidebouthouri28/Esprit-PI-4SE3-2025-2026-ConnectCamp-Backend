package tn.esprit.projetintegre.enums;

/**
 * Types of ML-driven suggestions for chat room owners based on sentiment analysis.
 */
public enum SuggestionType {
    // Moderation Actions
    ADD_MODERATOR("Add Moderator", "Consider adding more moderators to help manage discussions"),
    REMOVE_MODERATOR("Remove Moderator", "Review moderator effectiveness"),
    
    // Member Management
    BAN_MEMBER("Ban Member", "A member shows consistently toxic behavior"),
    MUTE_MEMBER("Mute Member", "Consider temporarily muting a disruptive member"),
    WARN_MEMBER("Warn Member", "A member needs a warning about their behavior"),
    PROMOTE_MEMBER("Promote Member", "A member contributes very positively - consider promoting them"),
    
    // Content Actions
    PIN_POSITIVE_MESSAGE("Pin Positive Message", "Highlight positive content to encourage good vibes"),
    DELETE_NEGATIVE_THREAD("Delete Negative Thread", "A conversation thread has become too toxic"),
    ARCHIVE_CHAT("Archive Chat", "Consider archiving inactive or problematic chats"),
    
    // Business/Monetization
    ADD_SPONSOR("Add Sponsor", "Positive sentiment makes this a good time for sponsorship"),
    CREATE_PREMIUM_TIER("Create Premium Tier", "High engagement - consider premium features"),
    HOST_EVENT("Host Event", "Great community vibes - perfect time for an event"),
    
    // Community Health
    POST_COMMUNITY_GUIDELINES("Post Guidelines", "Sentiment declining - remind members of rules"),
    START_POSITIVE_TOPIC("Start Positive Topic", "Chat needs a positivity boost - start a fun discussion"),
    CELEBRATE_MILESTONE("Celebrate Milestone", "Community is thriving - celebrate together!"),
    
    // Alert/Warnings
    SENTIMENT_DECLINING("Alert: Sentiment Declining", "Negative trend detected - take action soon"),
    SENTIMENT_CRISIS("Alert: Sentiment Crisis", "Chat is becoming very toxic - urgent action needed"),
    HIGH_TOXICITY_DETECTED("Alert: High Toxicity", "Multiple toxic users detected"),
    
    // Engagement
    INVITE_MORE_MEMBERS("Invite More Members", "Low activity - consider inviting new members"),
    REACTIVATE_CHAT("Reactivate Chat", "Chat has been quiet - spark a conversation"),
    CLOSE_CHAT("Close Chat", "Extremely toxic environment - consider closing permanently");

    private final String displayName;
    private final String defaultDescription;

    SuggestionType(String displayName, String defaultDescription) {
        this.displayName = displayName;
        this.defaultDescription = defaultDescription;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultDescription() {
        return defaultDescription;
    }
}
