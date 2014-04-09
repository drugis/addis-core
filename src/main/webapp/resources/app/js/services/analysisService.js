'use strict';
define(['angular'], function() {
  var dependencies = ['$location', '$stateParams', 'ProblemResource', 'ScenarioResource'];
  var AnalysisService = function($location, $stateParams, ProblemResource, ScenarioResource) {

    var getDefaultScenario = function(analysis) {
      console.log('getDefaultScenario');
      return ScenarioResource.query(analysis.id).then(function(scenarios) {
        console.log('query');
        return scenarios[0];
      });
    };

    var navigate = function(scenario) {
      console.log('navigate');
      return $location.url() + '/scenarios/' + scenario.id;
    };

    return {
      createProblem: function(analysis) {
        console.log('createProblem');

        var problemPromise = ProblemResource.get($stateParams).$promise;
        problemPromise.then(function() {
          var analysisMock = analysis.$save();
          var analysisPromise = analysisMock.$promise;
          analysisPromise.then(function(analysis) {
            var scenarioPromise = getDefaultScenario(analysis);
            var url;
            scenarioPromise.then(function(scenario) {
              url = navigate(scenario);
            });
          });
        });


        // return ProblemResource.get($stateParams).$promise
        //   .then(analysis.$save).$promise
        //   .then(getDefaultScenario)
        //   .then(navigate);
      }

    };
  };
  return dependencies.concat(AnalysisService);
});