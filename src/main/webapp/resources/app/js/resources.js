'use strict';
define(function(require) {
  var angular = require('angular', '');
  return angular.module('addis.resources', ['ngResource'])
    .factory('ProjectsService', require('services/projectsService'))
    .factory('TrialverseService', require('services/trialverseService'))
    .factory('SemanticOutcomeService', require('services/semanticOutcomeService'))
    .factory('SemanticInterventionService', require('services/semanticInterventionService'))
    .factory('OutcomeService', require('services/outcomeService'))
    .factory('InterventionService', require('services/interventionService'))
    .factory('AnalysisService', require('services/analysisService'));
});
