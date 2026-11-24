export type UserRole = 'ADMIN' | 'SUPPORT' | 'USER';

export const USER_ROLES: UserRole[] = ['ADMIN', 'SUPPORT', 'USER'];

export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  isActive: boolean;
  role: UserRole;
  createdAt: string;
  updatedAt: string;
}

export interface UserCreateInput {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  role?: UserRole;
}

export interface UserUpdateInput {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  password?: string;
}
