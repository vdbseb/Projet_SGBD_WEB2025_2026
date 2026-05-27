import {inject, Injectable} from '@angular/core';
import {PadelCourt, PadelSite} from '../shared/site.model';
import {HttpClient} from '@angular/common/http';
import {map, Observable} from 'rxjs';

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
}

@Injectable({
  providedIn: 'root'
})
export class PadelService {
  private readonly httpClient = inject(HttpClient);
  private readonly sitesUrl = 'http://localhost:8080/api/sites';
  private readonly courtsUrl = 'http://localhost:8080/api/courts';
  private readonly apiBaseUrl = 'http://localhost:8080/api';


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
  getCourts(): Observable<CourtDTO[]> {
    return this.httpClient.get<CourtDTO[]>(this.courtsUrl);
  }

  private toPadelCourt(court: CourtDTO): PadelCourt {
    const type = court.type ?? (court.indoor ? 'Indoor' : 'Outdoor');

    return {
      id: court.id,
      name: court.name,
      type,
      active: court.active ?? true,
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
  getReservations(courtId: number, date: string) {
    return this.httpClient.get<any[]>(
      `${this.apiBaseUrl}/reservations?courtId=${courtId}&date=${date}`
    );
  }

  createReservation(reservation: any) {
    return this.httpClient.post(
      `${this.apiBaseUrl}/reservations`,
      reservation
    );
  }

  getMembers() {
    return this.httpClient.get<any[]>(
      `${this.apiBaseUrl}/members`
    );
  }
  getMemberByMatricule(matricule: string) {
    return this.httpClient.get<any>(
      `${this.apiBaseUrl}/members/${matricule}`
    );
  }
  getAllReservations() {
    return this.httpClient.get<any[]>(
      `${this.apiBaseUrl}/reservations`
    );
  }
  deleteReservation(id: number) {
    return this.httpClient.delete(
      `${this.apiBaseUrl}/reservations/${id}`
    );
  }
  getAdministrators() {
    return this.httpClient.get<any[]>(
      `${this.apiBaseUrl}/administrateurs`
    );
  }
  getMatches() {
    return this.httpClient.get<any[]>(
      `${this.apiBaseUrl}/matches`
    );
  }

  joinPublicMatch(matchId: number, memberId: number) {
    return this.httpClient.post<void>(
      `${this.apiBaseUrl}/matches/${matchId}/join/${memberId}`,
      null
    );
  }
}


  /*
  private readonly sites: PadelSite[] = [
    {
      id: uuid(),
      city: 'Bruxelles',
      clubName: 'The Atomium Padel Club',
      image: 'images/bruxelles.jpg',
      initial: 'B',
      description: 'Situé au cœur de la capitale, ce centre propose des terrains indoor de dernière génération. Idéal pour une partie entre collègues ou un tournoi intensif.',
      courts:[
        {id : uuid(), name: 'Court1', type: 'Indoor'},
        {id : uuid(), name: 'Court2', type: 'Outdoor'}
      ]
    },
    {
      id: uuid(),
      city: 'Liège',
      clubName: 'The Carré Club',
      image: 'images/liege.jpg',
      initial: 'L',
      description: 'La "Cité Ardente" porte bien son nom ! Profitez de terrains spacieux et d\'un club-house réputé pour sa convivialité et son ambiance unique.',
      courts:[
        {id : uuid(), name: 'Court1', type: 'Outdoor'},
        {id : uuid(), name: 'Court2', type: 'Indoor'}
      ]
    },
    {
      id: uuid(),
      city: 'Arlon',
      clubName: 'Arlon Green Padel',
      image: 'images/arlon.jpg',
      initial: 'A',
      description: 'À la frontière du Luxembourg, ce site offre un cadre verdoyant et apaisant. Des installations modernes parfaites pour s\'évader du quotidien.',
      courts:[
        {id : uuid(), name: 'Court1', type: 'Indoor'},
        {id : uuid(), name: 'Court2', type: 'Outdoor'}
      ]
    }
  ];
  getSites(): PadelSite[] {
    return this.sites;
  }
  getSiteById(id: string): PadelSite | undefined {
    return this.sites.find(site => site.id === id);
  }
}
*/
