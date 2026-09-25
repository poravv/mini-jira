package com.minijira.issue.service;

import com.minijira.issue.entity.IssuePriority;
import com.minijira.issue.entity.IssueStatus;

import java.util.EnumSet;
import java.util.Map;

/** Reglas E1-E6 y P1-P5 del workflow de incidencias. */
final class IssueTransitionPolicy {

    private static final Map<IssueStatus, EnumSet<IssueStatus>> ALLOWED_STATUS_TRANSITIONS = Map.of(
            IssueStatus.PENDIENTE, EnumSet.of(IssueStatus.PENDIENTE, IssueStatus.EN_PROGRESO, IssueStatus.CERRADA),
            IssueStatus.EN_PROGRESO, EnumSet.of(IssueStatus.EN_PROGRESO, IssueStatus.PENDIENTE, IssueStatus.RESUELTA),
            IssueStatus.RESUELTA, EnumSet.of(IssueStatus.RESUELTA, IssueStatus.EN_PROGRESO, IssueStatus.CERRADA),
            IssueStatus.CERRADA, EnumSet.of(IssueStatus.CERRADA)
    );

    private IssueTransitionPolicy() {
    }

    static boolean isStatusTransitionAllowed(IssueStatus currentStatus, IssueStatus requestedStatus) {
        if (currentStatus == null || requestedStatus == null) {
            return false;
        }
        EnumSet<IssueStatus> allowedDestinations = ALLOWED_STATUS_TRANSITIONS.get(currentStatus);
        return allowedDestinations != null && allowedDestinations.contains(requestedStatus);
    }

    static boolean isPriorityTransitionAllowed(IssuePriority currentPriority, IssuePriority requestedPriority) {
        // P1-P5 permiten cambiar entre todas las prioridades y conservar el mismo valor.
        return currentPriority != null && requestedPriority != null;
    }
}
