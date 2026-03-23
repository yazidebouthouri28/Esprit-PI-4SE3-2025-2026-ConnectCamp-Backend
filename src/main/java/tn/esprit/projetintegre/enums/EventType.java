package tn.esprit.projetintegre.enums;

/**
 * Types d'événements organisés.
 */
public enum EventType {
    WORKSHOP,
    CONFERENCE,
    FESTIVAL,
    TRIP,
    OUTDOOR_ACTIVITY,
    CAMPING,
    HIKING,
    CONCERT,
    EXHIBITION,
    SPORTS,
    SOCIAL,
    OTHER;

    public static EventType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return EventType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return OTHER;
        }
    }
}
