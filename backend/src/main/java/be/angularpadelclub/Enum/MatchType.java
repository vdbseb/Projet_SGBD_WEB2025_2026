package be.angularpadelclub.Enum;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum MatchType {
    PUBLIC,
    PRIVE;

    @JsonCreator
    public static MatchType fromString(String value) {

        if (value == null) {
            return null;
        }

        return switch (value.toUpperCase()) {
            case "PRIVATE", "PRIVE" -> PRIVE;
            case "PUBLIC" -> PUBLIC;
            default -> throw new IllegalArgumentException(
                    "Type match invalide : " + value
            );
        };
    }
}