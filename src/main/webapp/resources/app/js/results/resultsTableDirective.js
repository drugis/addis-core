'use strict';
define([], function() {
  var dependencies = ['$q', '$stateParams', 'ResultsTableService', 'ArmService', 'MeasurementMomentService'];

  var resultsTableDirective = function($q, $stateParams, ResultsTableService, ArmService, MeasurementMomentService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/results/resultsTableDirective.html',
      scope: {
        variable: '=',
        arms: '=',
        measurementMoments: '='
      },
      link: function(scope) {
        $q.all([scope.arms, scope.measurementMoments]).then(function() {
          scope.inputRows = ResultsTableService.createInputRows(scope.variable, scope.arms, scope.measurementMoments);
          scope.inputHeaders = ResultsTableService.createHeaders(scope.variable.measurementType);
        });

      }
    };
  };

  return dependencies.concat(resultsTableDirective);
});
