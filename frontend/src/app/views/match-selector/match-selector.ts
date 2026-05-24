import {Component, inject, input, model, output, signal} from '@angular/core';
import {PadelService} from '../../services/padel.service';

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
  private padelService = inject(PadelService);

  participantMatricules = signal<string[]>(['', '', '']);
  invalidParticipants = signal<string[]>([]);

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
    const invalids = [...this.invalidParticipants()];

    if (!normalizedValue) {
      participants[index] = '';
      invalids[index] = '';
      this.participantMatricules.set(participants);
      this.invalidParticipants.set(invalids);
      this.emitState();
      return;
    }

    if (normalizedValue === currentMatricule) {
      participants[index] = '';
      invalids[index] = '';
      this.participantMatricules.set(participants);
      this.invalidParticipants.set(invalids);
      this.emitState();
      return;
    }

    const duplicate = participants.some(
      (participant, i) => i !== index && participant === normalizedValue
    );

    if (duplicate) {
      participants[index] = '';
      invalids[index] = '';
      this.participantMatricules.set(participants);
      this.invalidParticipants.set(invalids);
      this.emitState();
      return;
    }

    this.padelService.getMemberByMatricule(normalizedValue).subscribe({
      next: () => {
        participants[index] = normalizedValue;
        invalids[index] = '';

        this.participantMatricules.set(participants);
        this.invalidParticipants.set(invalids);

        this.emitState();
      },
      error: () => {
        participants[index] = normalizedValue;
        invalids[index] = normalizedValue;

        this.participantMatricules.set(participants);
        this.invalidParticipants.set(invalids);

        this.emitState();
      }
    });
  }

  emitState() {
    const filledParticipants = this.participantMatricules().filter(p => p !== '');
    const hasInvalidParticipants = this.invalidParticipants().some(p => p !== '');

    this.participantsChanged.emit(filledParticipants);

    this.validityChanged.emit(
      this.matchType() === 'PUBLIC' ||
      (filledParticipants.length === 3 && !hasInvalidParticipants)
    );
  }

  isPrivateIncomplete(): boolean {
    return this.matchType() === 'PRIVATE'
      && this.participantMatricules().filter(p => p !== '').length < 3;
  }
}
