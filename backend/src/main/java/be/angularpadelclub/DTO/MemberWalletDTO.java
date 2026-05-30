package be.angularpadelclub.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record MemberWalletDTO(
        @NotNull(message = "L'identifiant du membre est obligatoire.")
        Integer memberId,

        @NotBlank(message = "Le matricule du membre est obligatoire.")
        String matricule,

        @NotBlank(message = "Le nom affiché du membre est obligatoire.")
        String displayName,

        @NotBlank(message = "La devise est obligatoire.")
        String currency,

        @PositiveOrZero(message = "Le montant dû doit être positif ou nul.")
        Integer amountDueCentimes,

        @PositiveOrZero(message = "Le montant en attente doit être positif ou nul.")
        Integer amountPendingCentimes,

        @PositiveOrZero(message = "Le montant payé doit être positif ou nul.")
        Integer amountPaidCentimes,

        @PositiveOrZero(message = "Le montant remboursé doit être positif ou nul.")
        Integer amountRefundedCentimes,

        Integer creditCentimes,

        Integer balanceCentimes,

        List<@Valid DetteMembreDTO> openDebts
) {
}