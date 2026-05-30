import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';

import { PadelCourt, PadelSite } from '../shared/site.model';

interface SiteDTO {
  id: number;
  name: string;
  city: string;
  openingTime: string;
  closingTime: string;
  active: boolean;
  adresse: string;
  description: string;
  imageURL: string;
  courts: CourtDTO[];
}

interface CourtDTO {
  id: number;
  name: string;
  type?: 'Indoor' | 'Outdoor';
  indoor?: boolean;
  siteId: number;
  active: boolean;
  maintenance: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class PadelService {
  private readonly httpClient = inject(HttpClient);

  private readonly apiBaseUrl = 'http://localhost:8080/api';
  private readonly sitesUrl = `${this.apiBaseUrl}/sites`;
  private readonly courtsUrl = `${this.apiBaseUrl}/courts`;

  // =========================
  // SITES
  // =========================

  getSites(): Observable<PadelSite[]> {
    return this.httpClient.get<SiteDTO[]>(this.sitesUrl).pipe(
      map(sites => sites.map(site => this.toPadelSite(site, [])))
    );
  }

  getSiteById(id: number): Observable<PadelSite> {
    return this.httpClient.get<SiteDTO>(`${this.sitesUrl}/${id}`).pipe(
      map(site => this.toPadelSite(site, []))
    );
  }

  deactivateSite(siteId: number) {
    return this.httpClient.delete<void>(
      `${this.apiBaseUrl}/sites/${siteId}`
    );
  }

  updateSite(siteId: number, site: any) {
    return this.httpClient.put<any>(
      `${this.apiBaseUrl}/sites/${siteId}`,
      site
    );
  }

  // =========================
  // TERRAINS
  // =========================

  getCourts(): Observable<CourtDTO[]> {
    return this.httpClient.get<CourtDTO[]>(this.courtsUrl);
  }

setCourtMaintenance(courtId: number, maintenance: boolean) {
  return this.httpClient.patch<CourtDTO>(
    `${this.courtsUrl}/${courtId}/maintenance`,
    null,
    {
      params: {
        maintenance
      }
    }
  );
}

  // =========================
  // RÉSERVATIONS
  // =========================

  getReservations(courtId: number, date: string) {
    return this.httpClient.get<any[]>(
      `${this.apiBaseUrl}/reservations?courtId=${courtId}&date=${date}`
    );
  }

  getAllReservations() {
    return this.httpClient.get<any[]>(
      `${this.apiBaseUrl}/reservations`
    );
  }

  createReservation(reservation: any) {
    return this.httpClient.post<any>(
      `${this.apiBaseUrl}/reservations`,
      reservation
    );
  }

  deleteReservation(id: number, memberId?: number) {
    const url = memberId
      ? `${this.apiBaseUrl}/reservations/${id}?memberId=${memberId}`
      : `${this.apiBaseUrl}/reservations/${id}`;

    return this.httpClient.delete<void>(url);
  }

  // =========================
  // MEMBRES
  // =========================

  getMembers() {
    return this.httpClient.get<any[]>(
      `${this.apiBaseUrl}/members`
    );
  }

  getMembersForAdmin(adminMatricule: string) {
    return this.httpClient.get<any[]>(
      `${this.apiBaseUrl}/members/admin/${adminMatricule}`
    );
  }

  getMemberByMatricule(matricule: string) {
    return this.httpClient.get<any>(
      `${this.apiBaseUrl}/members/${matricule}`
    );
  }

  getNextMatricule(typeCode: string) {
    return this.httpClient.get(
      `${this.apiBaseUrl}/members/next-matricule`,
      {
        params: { typeCode },
        responseType: 'text'
      }
    );
  }

  createMember(member: any) {
    return this.httpClient.post<any>(
      `${this.apiBaseUrl}/members`,
      member
    );
  }

  createMemberAsAdmin(adminMatricule: string, member: any) {
    return this.httpClient.post<any>(
      `${this.apiBaseUrl}/members/admin/${adminMatricule}`,
      member
    );
  }

  updateMemberActiveStatus(memberId: number, active: boolean) {
    return this.httpClient.patch<any>(
      `${this.apiBaseUrl}/members/${memberId}/active`,
      { active }
    );
  }

  updateOwnMemberProfile(memberId: number, profile: { firstName: string | null; lastName: string | null; email: string | null }) {
    return this.httpClient.patch<any>(
      `${this.apiBaseUrl}/members/${memberId}/profile`,
      profile
    );
  }

  getMemberWallet(memberId: number) {
    return this.httpClient.get<any>(
      `${this.apiBaseUrl}/paiements/member/${memberId}/wallet`
    );
  }

  initierMemberDebtsPayment(memberId: number) {
    return this.httpClient.post<any>(
      `${this.apiBaseUrl}/paiements/member/${memberId}/dettes/initier`,
      null
    );
  }

getActiveMemberPenalties(memberId: number) {
  return this.httpClient.get<any[]>(
    `${this.apiBaseUrl}/penalites/member/${memberId}/active`
  );
}

  // =========================
  // ADMINISTRATEURS
  // =========================

  getAdministrators() {
    return this.httpClient.get<any[]>(
      `${this.apiBaseUrl}/administrateurs`
    );
  }

  // =========================
  // MATCHS
  // =========================

  getMatches() {
    return this.httpClient.get<any[]>(
      `${this.apiBaseUrl}/matches`
    );
  }

  joinPublicMatch(matchId: number, memberId: number) {
    return this.httpClient.post<any>(
      `${this.apiBaseUrl}/matches/${matchId}/join/${memberId}`,
      null
    );
  }

  leavePublicMatch(matchId: number, memberId: number) {
    return this.httpClient.delete<void>(
      `${this.apiBaseUrl}/matches/${matchId}/leave/${memberId}`
    );
  }

  // =========================
  // HORAIRES / FERMETURES
  // =========================

  getSiteSchedule(siteId: number, year: number) {
    return this.httpClient.get<any>(
      `${this.apiBaseUrl}/horaires-sites/site/${siteId}/annee/${year}`
    );
  }

  getSiteClosingDays(siteId: number) {
    return this.httpClient.get<any[]>(
      `${this.apiBaseUrl}/jours-fermeture/site/${siteId}`
    );
  }

  getGlobalClosingDays() {
    return this.httpClient.get<any[]>(
      `${this.apiBaseUrl}/jours-fermeture/globales`
    );
  }

  // =========================
  // PAIEMENTS
  // =========================

  getPayments() {
    return this.httpClient.get<any[]>(
      `${this.apiBaseUrl}/paiements`
    );
  }

  confirmPayment(paymentId: number) {
    return this.httpClient.patch<any>(
      `${this.apiBaseUrl}/paiements/${paymentId}/confirmer`,
      null
    );
  }

  refusePayment(paymentId: number) {
    return this.httpClient.patch<any>(
      `${this.apiBaseUrl}/paiements/${paymentId}/refuser`,
      null
    );
  }

  refundPayment(paymentId: number) {
    return this.httpClient.patch<any>(
      `${this.apiBaseUrl}/paiements/${paymentId}/rembourser`,
      null
    );
  }

initierPaiementPourParticipation(participationId: number) {
  return this.httpClient.post<any>(
    `${this.apiBaseUrl}/paiements/participation/${participationId}/initier`,
    {}
  );
}

  // =========================
  // MAPPERS FRONT
  // =========================

  private toPadelCourt(court: CourtDTO): PadelCourt {
    const type = court.type ?? (court.indoor ? 'Indoor' : 'Outdoor');

    return {
      id: court.id,
      name: court.name,
      type,
      active: court.active ?? true,
      maintenance: court.maintenance ?? false,
      siteId: court.siteId
    };
  }

  private toPadelSite(site: SiteDTO, courts: PadelCourt[]): PadelSite {
    return {
      id: site.id,
      city: site.city,
      clubName: site.name,
      image: site.imageURL || this.getImageForCity(site.city),
      initial: site.city.charAt(0).toUpperCase(),
      description: site.description,
      courts: site.courts?.map(court => this.toPadelCourt(court)) || courts,
      openingTime: site.openingTime,
      closingTime: site.closingTime,
      active: site.active,
      adresse: site.adresse
    } as any;
  }

  private getImageForCity(city: string): string {
    switch (city.toLowerCase()) {
      case 'bruxelles':
        return 'images/bruxelles.jpg';
      case 'liège':
      case 'liege':
        return 'images/liege.jpg';
      case 'arlon':
        return 'images/arlon.jpg';
      default:
        return 'images/bruxelles.jpg';
    }
  }
}
