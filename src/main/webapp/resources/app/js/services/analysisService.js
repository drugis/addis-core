'use strict';
define(['angular'], function() {
  var dependencies = ['$location', '$stateParams', '$q', 'ProblemResource', 'ScenarioResource'];
  var AnalysisService = function($location, $stateParams, $q, ProblemResource, ScenarioResource) {

    var getDefaultScenario = function(analysis) {
      console.log('getDefaultScenario');
      return ScenarioResource.query(analysis.id).$promise.then(function(scenarios) {
        return scenarios[0];
      });
    };

    var navigate = function(scenario) {
        $location.url($location.url() + '/scenarios/' + scenario.id);
    };

    return {
      createProblem: function(analysis) {
        return ProblemResource.get($stateParams).$promise
          .then(analysis.$save)
          .then(getDefaultScenario)
          .then(navigate);
      }

    };
  };
  return dependencies.concat(AnalysisService);
});