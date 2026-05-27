export interface PadelCourt {
  id: number;
  name: string;
  type: 'Indoor' | 'Outdoor';
  active: boolean;
  siteId?: number;
}

export interface PadelSite {
  id: number;
  city: string;
  clubName: string;
  image: string;
  initial: string;
  description: string;
  courts: PadelCourt[];

  openingTime: string;
  closingTime: string;
  active: boolean;
  adresse?: string;
}
