'use strict';
define([], function() {
  var dependencies = ['$q', '$injector', '$stateParams', 'ArmService', 'MeasurementMomentService', 'ResultsService'];

  var resultsTableListDirective = function($q, $injector, $stateParams, ArmService, MeasurementMomentService, ResultsService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/results/resultsTableListDirective.html',
      scope: {
        variableType: '=',
        variableName: '='
      },
      link: function(scope) {
        var variableService = $injector.get(scope.variableType + 'Service');
        var variablesPromise, armsPromise;
        scope.showResults = false;

        scope.$on('refreshResults', function(event, args){
          console.log('now cleanUpMeasurements and reload tha tables');
          ResultsService.cleanUpMeasurements().then(reloadResultTables);
        });

        function reloadResultTables() {

          armsPromise = ArmService.queryItems($stateParams.studyUUID).then(function(result) {
            scope.arms = result;
            return result;
          });

          scope.measurementMoments = MeasurementMomentService.queryItems($stateParams.studyUUID).then(function(result) {
            scope.measurementMoments = result;
            return result;
          });

          variablesPromise = variableService.queryItems($stateParams.studyUUID).then(function(result) {
            scope.variables = result;
          });

          $q.all([armsPromise, variablesPromise]).then(function() {
            var isAnyMeasuredVariable = _.find(scope.variables, function(variable) {
              return variable.measuredAtMoments.length > 0;
            });
            scope.showResults = isAnyMeasuredVariable && scope.arms.length > 0;
          });
        }

        reloadResultTables();

      }
    };
  };

  return dependencies.concat(resultsTableListDirective);
});
