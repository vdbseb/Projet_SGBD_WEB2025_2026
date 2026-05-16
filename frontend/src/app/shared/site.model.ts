export type UUID = string;

export interface PadelCourt {
  id: string;
  name: string;
  type: 'Indoor' | 'Outdoor';
}

export interface PadelSite {
  id: UUID;
  city: string;
  clubName: string;
  description: string;
  image: string;
  initial: string;
  courts: PadelCourt[];
}
