package be.angularpadelclub.Enum;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum MatchType {
    PUBLIC,
    PRIVE;

    @JsonCreator
    public static MatchType fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalizedValue = value
                .trim()
                .toUpperCase()
                .replace("É", "E")
                .replace("È", "E")
                .replace("Ê", "E");

        return switch (normalizedValue) {
            case "PRIVATE", "PRIVE" -> PRIVE;
            case "PUBLIC" -> PUBLIC;
            default -> throw new IllegalArgumentException(
                    "Type de match invalide : " + value + ". Valeurs acceptées : PUBLIC, PRIVE."
            );
        };
    }
}