import { ISSUE_PRIORITIES, IssuePriority, IssueStatus } from './issue.model';

/**
 * Reglas E1-E6 del workflow de una incidencia. CERRADA es un estado terminal.
 * Se mantienen en un único lugar para que la interfaz nunca ofrezca una acción
 * que el backend rechazaría.
 */
const ALLOWED_STATUS_TRANSITIONS: Readonly<Record<IssueStatus, readonly IssueStatus[]>> = {
  PENDIENTE: ['EN_PROGRESO', 'CERRADA'],
  EN_PROGRESO: ['PENDIENTE', 'RESUELTA'],
  RESUELTA: ['EN_PROGRESO', 'CERRADA'],
  CERRADA: []
};

export function getAllowedStatusTransitions(status: IssueStatus): readonly IssueStatus[] {
  return ALLOWED_STATUS_TRANSITIONS[status];
}

/**
 * Las reglas P1-P5 permiten cualquier prioridad válida, incluso conservar la
 * actual. La interfaz omite esta última porque no supondría ningún cambio.
 */
export function getAllowedPriorityTransitions(priority: IssuePriority): readonly IssuePriority[] {
  return ISSUE_PRIORITIES.filter((candidate) => candidate !== priority);
}
