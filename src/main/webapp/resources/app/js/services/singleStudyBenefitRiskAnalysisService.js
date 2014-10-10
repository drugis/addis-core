'use strict';
define(['angular'], function() {
  var dependencies = ['$location', '$stateParams', '$q', 'ProblemResource', 'ScenarioResource'];
  var SingleStudyBenefitRiskAnalysisService = function($location, $stateParams, $q, ProblemResource, ScenarioResource) {

    var getDefaultScenario = function() {
      return ScenarioResource
        .query(_.omit($stateParams, 'id'))
        .$promise
        .then(function(scenarios) {
          return scenarios[0];
        });
    };

    var validateAnalysis = function(analysis) {
      return analysis.selectedInterventions.length >= 2 &&
        analysis.selectedOutcomes.length >= 2;
    };

    var verifyCell = function(performanceEntry, outcome, intervention) {
      var result = performanceEntry.criterionUri === outcome.semanticOutcomeUri && performanceEntry.alternativeUri === intervention.semanticInterventionUri;
      return result;
    };

    var findPerformanceEntry = function(performanceTable, outcome, intervention) {
      var result = _.find(performanceTable, function(performanceEntry) {
        return verifyCell(performanceEntry, outcome, intervention);
      });
      return result;
    };

    var checkAllInterventionsForOutcome = function(performanceTable, outcome, selectedInterventions) {
      var result = _.every(selectedInterventions, function(intervention) {
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

    var concatWithNoDuplicates = function(source, target, comparatorFunction) {
      var complement = findMissing(source, target, comparatorFunction);
      return complement.concat(target);
    };

    var findMissing = function(searchList, optionList, comparatorFunction) {
      return _.filter(searchList, function(searchItem) {
        return !_.find(optionList, function(option) {
          return comparatorFunction(option, searchItem);
        });
      });
    };

    return {
      getProblem: getProblem,
      validateProblem: validateProblem,
      getDefaultScenario: getDefaultScenario,
      validateAnalysis: validateAnalysis,
      concatWithNoDuplicates: concatWithNoDuplicates,
      findMissing: findMissing
    };
  };
  return dependencies.concat(SingleStudyBenefitRiskAnalysisService);
});