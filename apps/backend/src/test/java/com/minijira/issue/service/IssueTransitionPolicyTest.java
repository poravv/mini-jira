package com.minijira.issue.service;

import com.minijira.issue.entity.IssuePriority;
import com.minijira.issue.entity.IssueStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IssueTransitionPolicyTest {

    @ParameterizedTest
    @MethodSource("allowedStatusTransitions")
    void should_allow_each_documented_status_transition(IssueStatus currentStatus, IssueStatus requestedStatus) {
        assertTrue(IssueTransitionPolicy.isStatusTransitionAllowed(currentStatus, requestedStatus));
    }

    @Test
    void should_reject_reopening_a_closed_issue() {
        assertFalse(IssueTransitionPolicy.isStatusTransitionAllowed(IssueStatus.CERRADA, IssueStatus.PENDIENTE));
        assertFalse(IssueTransitionPolicy.isStatusTransitionAllowed(IssueStatus.CERRADA, IssueStatus.EN_PROGRESO));
        assertFalse(IssueTransitionPolicy.isStatusTransitionAllowed(IssueStatus.CERRADA, IssueStatus.RESUELTA));
    }

    @Test
    void should_reject_a_transition_when_current_or_requested_status_is_missing() {
        assertFalse(IssueTransitionPolicy.isStatusTransitionAllowed(null, IssueStatus.PENDIENTE));
        assertFalse(IssueTransitionPolicy.isStatusTransitionAllowed(IssueStatus.PENDIENTE, null));
    }

    @Test
    void should_allow_all_priority_changes_and_same_priority() {
        for (IssuePriority currentPriority : IssuePriority.values()) {
            for (IssuePriority requestedPriority : IssuePriority.values()) {
                assertTrue(IssueTransitionPolicy.isPriorityTransitionAllowed(currentPriority, requestedPriority));
            }
        }
    }

    private static Stream<Arguments> allowedStatusTransitions() {
        return Stream.of(
                Arguments.of(IssueStatus.PENDIENTE, IssueStatus.PENDIENTE),
                Arguments.of(IssueStatus.PENDIENTE, IssueStatus.EN_PROGRESO),
                Arguments.of(IssueStatus.PENDIENTE, IssueStatus.CERRADA),
                Arguments.of(IssueStatus.EN_PROGRESO, IssueStatus.EN_PROGRESO),
                Arguments.of(IssueStatus.EN_PROGRESO, IssueStatus.PENDIENTE),
                Arguments.of(IssueStatus.EN_PROGRESO, IssueStatus.RESUELTA),
                Arguments.of(IssueStatus.RESUELTA, IssueStatus.RESUELTA),
                Arguments.of(IssueStatus.RESUELTA, IssueStatus.EN_PROGRESO),
                Arguments.of(IssueStatus.RESUELTA, IssueStatus.CERRADA),
                Arguments.of(IssueStatus.CERRADA, IssueStatus.CERRADA)
        );
    }
}
