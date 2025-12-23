export interface Player {
  id: number;
  username: string;
  email?: string;
  wallet?: number;       // montant total
  active?: boolean;
  createdAt?: string;
}
