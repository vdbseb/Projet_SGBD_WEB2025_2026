import { Component, input, model, output, signal } from '@angular/core';

export type MatchType = 'PRIVATE' | 'PUBLIC';

@Component({
  selector: 'app-match-selector',
  standalone: true,
  templateUrl: './match-selector.html'
})
export class MatchSelectorComponent {
  currentMember = input<any | null>(null);

  matchType = model<MatchType>('PRIVATE');

  participantsChanged = output<string[]>();
  validityChanged = output<boolean>();

  participantMatricules = signal<string[]>(['', '', '']);

  selectMatchType(type: MatchType) {
    this.matchType.set(type);
    this.participantMatricules.set(['', '', '']);

    this.participantsChanged.emit([]);
    this.validityChanged.emit(type === 'PUBLIC');
  }

  updateParticipant(index: number, value: string) {
    const normalizedValue = value.trim().toUpperCase();
    const currentMatricule = this.currentMember()?.matricule?.toUpperCase();

    const participants = [...this.participantMatricules()];

    if (normalizedValue === currentMatricule) {
      participants[index] = '';
      this.participantMatricules.set(participants);
      this.emitState();
      return;
    }

    const duplicate = participants.some(
      (participant, i) => i !== index && participant === normalizedValue
    );

    if (duplicate) {
      participants[index] = '';
      this.participantMatricules.set(participants);
      this.emitState();
      return;
    }

    participants[index] = normalizedValue;
    this.participantMatricules.set(participants);

    this.emitState();
  }

  emitState() {
    const filledParticipants = this.participantMatricules().filter(p => p !== '');

    this.participantsChanged.emit(filledParticipants);

    this.validityChanged.emit(
      this.matchType() === 'PUBLIC' || filledParticipants.length === 3
    );
  }

  isPrivateIncomplete(): boolean {
    return this.matchType() === 'PRIVATE'
      && this.participantMatricules().filter(p => p !== '').length < 3;
  }
}
