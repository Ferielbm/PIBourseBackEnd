export interface Wallet {
  id: number;
  playerId: number;
  balance: number;          // Solde total de la BDD
  reserved: number;         // Montant réservé (réservations actives)
  available: number;        // Disponible = balance - reserved
  currency: string;         // ex: "EUR"
  lastUpdated: string;
}
