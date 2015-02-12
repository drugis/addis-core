'use strict';
define([], function() {
  var dependencies = ['$injector', '$stateParams', 'ArmService', 'MeasurementMomentService'];

  var resultsTableListDirective = function($injector, $stateParams, ArmService, MeasurementMomentService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/results/resultsTableListDirective.html',
      scope: {
        variableType: '=',
        variableName: '='
      },
      link: function(scope) {
        var variableService = $injector.get(scope.variableType + 'Service');

        scope.arms = ArmService.queryItems($stateParams.studyUUID).then(function(result) {
          scope.arms = result;
        });
        scope.measurementMoments = MeasurementMomentService.queryItems($stateParams.studyUUID).then(function(result) {
          scope.measurementMoments = result;
        });
        scope.variables = variableService.queryItems($stateParams.studyUUID).then(function(result) {
          scope.variables = result;
        });
      }
    };
  };

  return dependencies.concat(resultsTableListDirective);
});
