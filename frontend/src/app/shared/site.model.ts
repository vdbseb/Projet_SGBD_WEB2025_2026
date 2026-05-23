export interface PadelCourt {
  id: number;
  name: string;
  type: 'Indoor' | 'Outdoor';
}

export interface PadelSite {
  id: number;
  city: string;
  clubName: string;
  description: string;
  image: string;
  initial: string;
  courts: PadelCourt[];
}
