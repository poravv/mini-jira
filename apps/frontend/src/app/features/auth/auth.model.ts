export type AuthRole = 'ADMIN' | 'SUPPORT' | 'USER';

export interface LoginInput {
  identifier: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: 'Bearer';
  userId: number;
  username: string;
  role: AuthRole;
}

export interface UserSession {
  accessToken: string;
  userId: number;
  username: string;
  role: AuthRole;
}
