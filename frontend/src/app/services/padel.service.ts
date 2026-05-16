import {inject, Injectable} from '@angular/core';
import { PadelSite } from '../shared/site.model';
import { uuid } from '../shared/uuid';
import {HttpClient} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class PadelService {
  httpClient: HttpClient = inject(HttpClient);
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
