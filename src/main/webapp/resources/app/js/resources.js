'use strict';
define(function (require) {
  var angular = require('angular', '');
  return angular.module('addis.resources', ['ngResource'])
    .factory('ProjectResource', require('resources/projectResource'))
    .factory('TrialverseResource', require('resources/trialverseResource'))
    .factory('SemanticOutcomeResource', require('resources/semanticOutcomeResource'))
    .factory('SemanticInterventionResource', require('resources/semanticInterventionResource'))
    .factory('OutcomeResource', require('resources/outcomeResource'))
    .factory('InterventionResource', require('resources/interventionResource'))
    .factory('AnalysisResource', require('resources/analysisResource'))
    .factory('TrialverseStudyResource', require('resources/trialverseStudyResource'))
    .factory('TrialverseTrialDataResource', require('resources/trialverseTrialDataResource'))
    .factory('TrialverseStudiesWithDetailsResource', require('resources/trialverseStudiesWithDetailsResource'))
    .factory('ScenarioResource', require('resources/scenarioResource'))
    .factory('StudyDetailsResource', require('resources/studyDetailsResource'))
    .factory('StudyDesignResource', require('resources/studyDesignResource'))
    .factory('StudyArmResource', require('resources/studyArmResource'))
    .factory('StudyEpochResource', require('resources/studyEpochResource'));
});