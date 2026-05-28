package be.angularpadelclub.DTO;

import java.util.List;

public record MemberWalletDTO(
        Integer memberId,
        String matricule,
        String displayName,
        String currency,
        Integer amountDueCentimes,
        Integer amountPendingCentimes,
        Integer amountPaidCentimes,
        Integer amountRefundedCentimes,
        Integer creditCentimes,
        Integer balanceCentimes,
        List<DetteMembreDTO> openDebts
) {
}
