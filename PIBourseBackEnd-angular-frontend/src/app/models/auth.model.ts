export interface User {
  playerId: number;
  username: string;
  email: string;
  role: 'ROLE_PLAYER' | 'ROLE_MENEUR_JEU' | 'ROLE_ADMIN';
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  role?: 'ROLE_PLAYER' | 'ROLE_MENEUR_JEU' | 'ROLE_ADMIN';
}

export interface AuthResponse {
  playerId: number;
  username: string;
  email: string;
  role: 'ROLE_PLAYER' | 'ROLE_MENEUR_JEU' | 'ROLE_ADMIN';
  token?: string;
  message: string;
}
