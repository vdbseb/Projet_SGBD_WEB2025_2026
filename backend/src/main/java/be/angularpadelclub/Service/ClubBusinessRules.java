package be.angularpadelclub.Service;

public final class ClubBusinessRules {

    public static final int MAX_PLAYERS_PER_MATCH = 4;

    public static final int DEFAULT_MATCH_PRICE_EUROS = 60;
    public static final int CENTS_PER_EURO = 100;
    public static final int DEFAULT_PLAYER_SHARE_CENTS = 1500;

    public static final int DEFAULT_MATCH_DURATION_MINUTES = 90;
    public static final int DEFAULT_PAUSE_MINUTES = 15;

    public static final int PENALTY_DURATION_DAYS = 7;

    public static final int GLOBAL_MEMBER_RESERVATION_WEEKS = 3;
    public static final int SITE_MEMBER_RESERVATION_WEEKS = 2;
    public static final int FREE_MEMBER_RESERVATION_DAYS = 5;

    private ClubBusinessRules() {
    }
}