// Tipos del recurso Issue: espejo del contrato JSON del backend (verificar en Swagger).
export type IssueStatus = 'PENDIENTE' | 'EN_PROGRESO' | 'RESUELTA' | 'CERRADA';
export type IssuePriority = 'BAJA' | 'MEDIA' | 'ALTA' | 'CRITICA';

export const ISSUE_STATUSES: IssueStatus[] = ['PENDIENTE', 'EN_PROGRESO', 'RESUELTA', 'CERRADA'];
export const ISSUE_PRIORITIES: IssuePriority[] = ['BAJA', 'MEDIA', 'ALTA', 'CRITICA'];

export interface Issue {
  id: number;
  title: string;
  description?: string;
  status: IssueStatus;
  priority: IssuePriority;
  createdAt: string;
  updatedAt: string;
}

/** Payload del POST: el backend asigna id y timestamps. */
export type IssueInput = Omit<Issue, 'id' | 'createdAt' | 'updatedAt'>;
