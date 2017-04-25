'use strict';
define(function (require) {
  var angular = require('angular');
  return angular.module('addis.resources', ['ngResource'])
    .factory('ProjectResource', require('resources/projectResource'))
    .factory('TrialverseResource', require('resources/trialverseResource'))
    .factory('SemanticOutcomeResource', require('resources/semanticOutcomeResource'))
    .factory('SemanticInterventionResource', require('resources/semanticInterventionResource'))
    .factory('OutcomeResource', require('resources/outcomeResource'))
    .factory('InterventionResource', require('resources/interventionResource'))
    .factory('AnalysisResource', require('resources/analysisResource'))
    .factory('TrialverseStudyResource', require('resources/trialverseStudyResource'))
    .factory('EvidenceTableResource', require('resources/evidenceTableResource'))
    .factory('ProjectStudiesResource', require('resources/projectStudiesResource'))
    .factory('TrialverseStudiesWithDetailsResource', require('resources/trialverseStudiesWithDetailsResource'))
    .factory('ScenarioResource', require('resources/scenarioResource'))
    .factory('StudyDetailsResource', require('resources/studyDetailsResource'))
    .factory('StudyDesignResource', require('resources/studyDesignResource'))
    .factory('StudyGroupResource', require('resources/studyGroupResource'))
    .factory('StudyEpochResource', require('resources/studyEpochResource'))
    .factory('StudyTreatmentActivityResource', require('resources/studyTreatmentActivityResource'))
    .factory('StudyPopulationCharacteristicsResource', require('resources/studyPopulationCharacteristicsResource'))
    .factory('StudyEndpointsResource', require('resources/studyEndpointsResource'))
    .factory('StudyAdverseEventsResource', require('resources/studyAdverseEventsResource'))
    .factory('EffectsTableResource', require('resources/effectsTableResource'));
    .factory('RemarksResource', require('resources/mcdaEffectsTableRemarksResource'))
    .factory('ScaledUnitResource', require('resources/scaledUnitResource'));
});
