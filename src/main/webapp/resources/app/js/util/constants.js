'use strict';
define(['lodash'], function(_) {
  return {
    GROUP_ALLOCATION_OPTIONS: _.keyBy([{
      uri: 'ontology:AllocationRandomized',
      label: 'Randomized'
    }, {
      uri: 'ontology:AllocationNonRandomized',
      label: 'Non-Randomized'
    }, {
      uri: 'unknown',
      label: 'Unknown'
    }], 'uri'),
    BLINDING_OPTIONS: _.keyBy([{
      uri: 'ontology:OpenLabel',
      label: 'Open'
    }, {
      uri: 'ontology:SingleBlind',
      label: 'Single blind'
    }, {
      uri: 'ontology:DoubleBlind',
      label: 'Double blind'
    }, {
      uri: 'ontology:TripleBlind',
      label: 'Triple blind'
    }, {
      uri: 'unknown',
      label: 'Unknown'
    }], 'uri'),
    STATUS_OPTIONS: _.keyBy([{
      uri: 'ontology:StatusRecruiting',
      label: 'Recruiting'
    }, {
      uri: 'ontology:StatusEnrolling',
      label: 'Enrolling'
    }, {
      uri: 'ontology:StatusActive',
      label: 'Active'
    }, {
      uri: 'ontology:StatusCompleted',
      label: 'Completed'
    }, {
      uri: 'ontology:StatusSuspended',
      label: 'Suspended'
    }, {
      uri: 'ontology:StatusTerminated',
      label: 'Terminated'
    }, {
      uri: 'ontology:StatusWithdrawn',
      label: 'Withdrawn'
    }, {
      uri: 'unknown',
      label: 'Unknown'
    }], 'uri')
  };
});