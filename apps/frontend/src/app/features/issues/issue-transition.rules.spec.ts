import { getAllowedPriorityTransitions, getAllowedStatusTransitions } from './issue-transition.rules';

describe('issue transition rules', () => {
  it('expone solamente los destinos de estado permitidos por E1-E6', () => {
    expect(getAllowedStatusTransitions('PENDIENTE')).toEqual(['EN_PROGRESO', 'CERRADA']);
    expect(getAllowedStatusTransitions('EN_PROGRESO')).toEqual(['PENDIENTE', 'RESUELTA']);
    expect(getAllowedStatusTransitions('RESUELTA')).toEqual(['EN_PROGRESO', 'CERRADA']);
    expect(getAllowedStatusTransitions('CERRADA')).toEqual([]);
  });

  it('permite cambiar una prioridad por cualquier otra prioridad válida', () => {
    expect(getAllowedPriorityTransitions('MEDIA')).toEqual(['BAJA', 'ALTA', 'CRITICA']);
    expect(getAllowedPriorityTransitions('CRITICA')).toEqual(['BAJA', 'MEDIA', 'ALTA']);
  });
});
