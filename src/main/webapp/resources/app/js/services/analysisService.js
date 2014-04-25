'use strict';
define(['angular'], function() {
  var dependencies = ['$location', '$stateParams', '$q', 'ProblemResource', 'ScenarioResource'];
  var AnalysisService = function($location, $stateParams, $q, ProblemResource, ScenarioResource) {

    var getDefaultScenario = function() {
      return ScenarioResource
        .query($stateParams)
        .$promise
        .then(function(scenarios) {
          return scenarios[0];
        });
    };

    var validateAnalysis = function(analysis) {
      return analysis.selectedInterventions.length >= 2 &&
        analysis.selectedOutcomes.length >= 2;
    };

    var keyify = function(input) {
      var output =  input.replace(/[^a-zA-Z0-9 ]/g, '').replace(/ /g, '-').toLowerCase();
      return output;
    };

    var verifyCell = function(performanceEntry, outcome, intervention) {
      var result = performanceEntry.criterion === keyify(outcome.name) && performanceEntry.alternative === keyify(intervention.name);
      return result;
    };

    var findPerformanceEntry = function(performanceTable, outcome, intervention) {
      var result =  _.find(performanceTable, function(performanceEntry) {
        return verifyCell(performanceEntry, outcome, intervention);
      });
      return result;
    };

    var checkAllInterventionsForOutcome = function(performanceTable, outcome, selectedInterventions) {
      var result =  _.every(selectedInterventions, function(intervention) {
        return findPerformanceEntry(performanceTable, outcome, intervention);
      });
      return result;
    };

    var validateProblem = function(analysis, problem) {
      var selectedOutcomes = analysis.selectedOutcomes;
      var selectedInterventions = analysis.selectedInterventions;
      return _.every(selectedOutcomes, function(outcome) {
        return checkAllInterventionsForOutcome(problem.performanceTable, outcome, selectedInterventions);
      });
    };

    var getProblem = function() {
      return ProblemResource.get($stateParams).$promise;
    };

    return {
      getProblem: getProblem,
      validateProblem: validateProblem,
      getDefaultScenario: getDefaultScenario,
      validateAnalysis: validateAnalysis,
      keyify: keyify
    };
  };
  return dependencies.concat(AnalysisService);
});