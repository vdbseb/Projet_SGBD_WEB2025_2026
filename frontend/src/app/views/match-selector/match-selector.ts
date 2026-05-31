import { NgClass } from '@angular/common';
import { Component, inject, input, model, output, signal } from '@angular/core';

import { PadelService } from '../../services/padel.service';
import { getHttpErrorUserMessage } from '../../shared/api-error.util';

export type MatchType = 'PRIVATE' | 'PUBLIC';

@Component({
  selector: 'app-match-selector',
  standalone: true,
  imports: [NgClass],
  templateUrl: './match-selector.html'
})
export class MatchSelectorComponent {
  currentMember = input<any | null>(null);

  matchType = model<MatchType>('PRIVATE');

  participantsChanged = output<string[]>();
  validityChanged = output<boolean>();

  private padelService = inject(PadelService);

  participantMatricules = signal<string[]>(['', '', '']);
  invalidParticipants = signal<string[]>([]);
  participantErrors = signal<string[]>(['', '', '']);
  technicalError = signal<string | null>(null);

  selectMatchType(type: MatchType) {
    this.matchType.set(type);
    this.participantMatricules.set(['', '', '']);
    this.invalidParticipants.set([]);
    this.participantErrors.set(['', '', '']);
    this.technicalError.set(null);

    this.participantsChanged.emit([]);
    this.validityChanged.emit(type === 'PUBLIC');
  }

  updateParticipant(index: number, value: string) {
    const normalizedValue = value.trim().toUpperCase();
    const currentMatricule = this.currentMember()?.matricule?.toUpperCase();

    const participants = [...this.participantMatricules()];
    const invalids = [...this.invalidParticipants()];
    const errors = [...this.participantErrors()];

    this.technicalError.set(null);

    if (!normalizedValue) {
      this.clearParticipantAtIndex(index, participants, invalids, errors);
      return;
    }

    if (normalizedValue === currentMatricule) {
      participants[index] = normalizedValue;
      invalids[index] = normalizedValue;
      errors[index] = 'Tu es déjà l’organisateur du match.';

      this.updateState(participants, invalids, errors);
      return;
    }

    const duplicate = participants.some(
      (participant, i) => i !== index && participant === normalizedValue
    );

    if (duplicate) {
      participants[index] = normalizedValue;
      invalids[index] = normalizedValue;
      errors[index] = 'Ce membre est déjà ajouté au match.';

      this.updateState(participants, invalids, errors);
      return;
    }

    this.padelService.getMemberByMatricule(normalizedValue).subscribe({
      next: () => {
        participants[index] = normalizedValue;
        invalids[index] = '';
        errors[index] = '';

        this.updateState(participants, invalids, errors);
      },
      error: error => {
        participants[index] = normalizedValue;
        invalids[index] = normalizedValue;

        if (error?.status === 404) {
          errors[index] = 'Membre introuvable avec ce matricule.';
        } else {
          errors[index] = 'Validation impossible pour le moment.';
          this.technicalError.set(getHttpErrorUserMessage(error));
        }

        this.updateState(participants, invalids, errors);
      }
    });
  }

  emitState() {
    const filledParticipants = this.participantMatricules()
      .filter(participant => participant !== '');

    const hasInvalidParticipants = this.invalidParticipants()
      .some(participant => participant !== '');

    this.participantsChanged.emit(filledParticipants);

    this.validityChanged.emit(
      this.matchType() === 'PUBLIC' ||
      (filledParticipants.length === 3 && !hasInvalidParticipants)
    );
  }

  isPrivateIncomplete(): boolean {
    return this.matchType() === 'PRIVATE'
      && this.participantMatricules().filter(participant => participant !== '').length < 3;
  }

  hasParticipantError(index: number): boolean {
    return !!this.participantErrors()[index];
  }

  getParticipantError(index: number): string {
    return this.participantErrors()[index] ?? '';
  }

  private clearParticipantAtIndex(
    index: number,
    participants: string[],
    invalids: string[],
    errors: string[]
  ) {
    participants[index] = '';
    invalids[index] = '';
    errors[index] = '';

    this.updateState(participants, invalids, errors);
  }

  private updateState(
    participants: string[],
    invalids: string[],
    errors: string[]
  ) {
    this.participantMatricules.set(participants);
    this.invalidParticipants.set(invalids);
    this.participantErrors.set(errors);
    this.emitState();
  }
}
