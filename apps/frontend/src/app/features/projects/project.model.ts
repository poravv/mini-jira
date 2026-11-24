import { User } from '../users/user.model';

export interface Project {
  id: number;
  name: string;
  description?: string;
  createdAt: string;
  updatedAt: string;
  members: User[];
}

export interface ProjectInput {
  name: string;
  description: string;
}

export interface AddProjectMemberInput {
  userId: number;
}
