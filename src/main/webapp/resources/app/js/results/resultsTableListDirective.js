'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$q', '$injector', '$stateParams', 'ArmService', 'MeasurementMomentService', 'GroupService'];

  var resultsTableListDirective = function($q, $injector, $stateParams, ArmService, MeasurementMomentService, GroupService) {
    return {
      restrict: 'E',
      templateUrl: './resultsTableListDirective.html',
      scope: {
        variableType: '=',
        variableName: '=',
        isEditingAllowed: '='
      },
      link: function(scope) {
        var deregisterRefreshListener;
        var variableService = $injector.get(scope.variableType + 'Service');
        var variablesPromise, armsPromise, groupsPromise, measurementMomentsPromise;
        scope.showResults = false;

        function reloadResultTables() {

          if(deregisterRefreshListener) {
            // stop listening while loading to prevent race conditions
            deregisterRefreshListener();
          }

          armsPromise = ArmService.queryItems($stateParams.studyUUID).then(function(result) {
            scope.arms = result;
            return result;
          });

          groupsPromise = GroupService.queryItems($stateParams.studyUUID).then(function(result) {
            scope.groups = result;
            return result;
          });

          measurementMomentsPromise = MeasurementMomentService.queryItems($stateParams.studyUUID).then(function(result) {
            scope.measurementMoments = result;
            return result;
          });

          variablesPromise = variableService.queryItems($stateParams.studyUUID).then(function(result) {
            scope.variables = result;
          });

          $q.all([armsPromise, groupsPromise, variablesPromise]).then(function() {
            var isAnyMeasuredVariable = _.find(scope.variables, function(variable) {
              return variable.measuredAtMoments.length > 0;
            });
            scope.showResults = isAnyMeasuredVariable && (scope.arms.length > 0 || scope.groups.length > 0);
          });

          $q.all([armsPromise, groupsPromise, measurementMomentsPromise, variablesPromise]).then(function() {
            // register listnener as the loading is now done
            deregisterRefreshListener = scope.$on('refreshResults', function() {
              reloadResultTables();
            });
          });
        }

        // initialize the directive
        reloadResultTables();

      }
    };
  };

  return dependencies.concat(resultsTableListDirective);
});
