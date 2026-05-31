package be.angularpadelclub.Service;

import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Entity.ParticipationEntity;
import be.angularpadelclub.Enum.MatchStatus;
import be.angularpadelclub.Enum.MatchType;
import be.angularpadelclub.Enum.ParticipationStatut;

import java.util.List;

public final class ClubBusinessRules {

    public static final int MAX_PLAYERS_PER_MATCH = 4;

    public static final int DEFAULT_MATCH_PRICE_EUROS = 60;
    public static final int CENTS_PER_EURO = 100;
    public static final int DEFAULT_PLAYER_SHARE_CENTS = 1500;

    public static final int DEFAULT_MATCH_DURATION_MINUTES = 90;
    public static final int DEFAULT_PAUSE_MINUTES = 15;

    public static final int PAYMENT_EXPIRATION_MINUTES = 15;

    public static final int PENALTY_DURATION_DAYS = 7;

    public static final int GLOBAL_MEMBER_RESERVATION_WEEKS = 3;
    public static final int SITE_MEMBER_RESERVATION_WEEKS = 2;
    public static final int FREE_MEMBER_RESERVATION_DAYS = 5;

    public static final List<ParticipationStatut> ACTIVE_PARTICIPATION_STATUSES = List.of(
            ParticipationStatut.EN_ATTENTE_PAIEMENT,
            ParticipationStatut.PAYEE
    );

    public static final List<MatchStatus> PUBLIC_CONVERTIBLE_MATCH_STATUSES = List.of(
            MatchStatus.PLANIFIE,
            MatchStatus.OUVERT,
            MatchStatus.COMPLET
    );

    public static final List<MatchStatus> PUBLIC_BILLABLE_MATCH_STATUSES = List.of(
            MatchStatus.PLANIFIE,
            MatchStatus.OUVERT,
            MatchStatus.COMPLET
    );

    private ClubBusinessRules() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static String normalizeMatricule(String matricule) {
        return matricule == null
                ? null
                : matricule.trim().toUpperCase();
    }

    public static List<String> normalizeParticipantMatricules(
            List<String> participantMatricules
    ) {
        if (participantMatricules == null) {
            return List.of();
        }

        return participantMatricules.stream()
                .filter(matricule -> !isBlank(matricule))
                .map(ClubBusinessRules::normalizeMatricule)
                .toList();
    }

    public static boolean isGlobalMemberMatricule(String matricule) {
        String normalizedMatricule = normalizeMatricule(matricule);
        return normalizedMatricule != null && normalizedMatricule.startsWith("G");
    }

    public static boolean isSiteMemberMatricule(String matricule) {
        String normalizedMatricule = normalizeMatricule(matricule);
        return normalizedMatricule != null && normalizedMatricule.startsWith("S");
    }

    public static boolean isFreeMemberMatricule(String matricule) {
        String normalizedMatricule = normalizeMatricule(matricule);
        return normalizedMatricule != null && normalizedMatricule.startsWith("L");
    }

    public static boolean isKnownMemberMatricule(String matricule) {
        return isGlobalMemberMatricule(matricule)
                || isSiteMemberMatricule(matricule)
                || isFreeMemberMatricule(matricule);
    }

    public static boolean isActiveParticipationStatus(ParticipationStatut statut) {
        return statut != null && ACTIVE_PARTICIPATION_STATUSES.contains(statut);
    }

    public static boolean isActiveParticipation(ParticipationEntity participation) {
        return participation != null
                && isActiveParticipationStatus(participation.getStatut());
    }

    public static boolean isPaidParticipation(ParticipationEntity participation) {
        return participation != null
                && participation.getStatut() == ParticipationStatut.PAYEE;
    }

    public static long countActiveParticipations(MatchEntity match) {
        if (match == null || match.getParticipations() == null) {
            return 0;
        }

        return match.getParticipations()
                .stream()
                .filter(ClubBusinessRules::isActiveParticipation)
                .count();
    }

    public static boolean isCancellableMatchStatus(MatchStatus statut) {
        return statut != MatchStatus.ANNULE && statut != MatchStatus.TERMINE;
    }

    public static boolean isPublicConvertibleMatch(MatchEntity match) {
        return match != null
                && match.getStatut() != null
                && PUBLIC_CONVERTIBLE_MATCH_STATUSES.contains(match.getStatut());
    }

    public static boolean isPublicBillableMatch(MatchEntity match) {
        return match != null
                && match.getStatut() != null
                && PUBLIC_BILLABLE_MATCH_STATUSES.contains(match.getStatut());
    }

    public static MatchStatus resolveMatchStatusAfterParticipantCount(
            MatchType matchType,
            int activeParticipantsCount
    ) {
        if (activeParticipantsCount >= MAX_PLAYERS_PER_MATCH) {
            return MatchStatus.COMPLET;
        }

        return matchType == MatchType.PUBLIC
                ? MatchStatus.OUVERT
                : MatchStatus.PLANIFIE;
    }
}