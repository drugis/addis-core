'use strict';
define([], function() {
  var dependencies = ['$q', '$injector', '$stateParams', 'ArmService', 'MeasurementMomentService'];

  var resultsTableListDirective = function($q, $injector, $stateParams, ArmService, MeasurementMomentService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/results/resultsTableListDirective.html',
      scope: {
        variableType: '=',
        variableName: '=',
        isEditingAllowed: '='
      },
      link: function(scope) {
        var refreshListener;
        var variableService = $injector.get(scope.variableType + 'Service');
        var variablesPromise, armsPromise, measurementMomentsPromise;
        scope.showResults = false;

        function reloadResultTables() {

          if(refreshListener) {
            // stop listening while loading to prevent race conditions
            refreshListener();
          }

          armsPromise = ArmService.queryItems($stateParams.studyUUID).then(function(result) {
            scope.arms = result;
            return result;
          });

          measurementMomentsPromise = MeasurementMomentService.queryItems($stateParams.studyUUID).then(function(result) {
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

          $q.all([armsPromise, measurementMomentsPromise, variablesPromise]).then(function() {
            // register listnener as the loading is now done
            refreshListener = scope.$on('refreshResults', function(event, args) {
              reloadResultTables();
            });
          })
        }

        // initialize the directive 
        reloadResultTables();

      }
    };
  };

  return dependencies.concat(resultsTableListDirective);
});