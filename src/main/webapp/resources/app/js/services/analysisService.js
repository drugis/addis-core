'use strict';
define(['angular'], function() {
  var dependencies = ['$state', '$stateParams', '$q', 'ProblemResource', 'ScenarioResource'];
  var AnalysisService = function($state, $stateParams, $q, ProblemResource, ScenarioResource) {

    var analysisCache;

    var getDefaultScenario = function(analysis) {
      return ScenarioResource.query($stateParams).$promise.then(function(scenarios) {
        return scenarios[0];
      });
    };

    var navigate = function(scenario) {
       $state.go('scenario', {scenarioId: scenario.id.toString()});
    };

    var saveAnalysis = function(problem) {
      analysisCache.problem = problem;
      return analysisCache.$save();
    }

    return {
      createProblem: function(analysis) {
        analysisCache = analysis;
        return ProblemResource.get($stateParams).$promise
          .then(saveAnalysis)
          .then(getDefaultScenario)
          .then(navigate);
      }

    };
  };
  return dependencies.concat(AnalysisService);
});