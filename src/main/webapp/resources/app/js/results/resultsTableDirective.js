'use strict';
define([], function() {
  var dependencies = ['$q', '$stateParams', 'ResultsTableService', 'ResultsService', 'ArmService',
   'MeasurementMomentService'];

  var resultsTableDirective = function($q, $stateParams, ResultsTableService, ResultsService, ArmService,
   MeasurementMomentService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/results/resultsTableDirective.html',
      scope: {
        variable: '=',
        arms: '=',
        measurementMoments: '=',
        isEditingAllowed: '='
      },
      link: function(scope) {

        scope.results = ResultsService.queryResults(scope.variable.uri);

        $q.all([scope.arms, scope.measurementMoments, scope.results]).then(function() {
          scope.inputRows = ResultsTableService.createInputRows(scope.variable, scope.arms, scope.measurementMoments, scope.results.$$state.value);
          scope.inputHeaders = ResultsTableService.createHeaders(scope.variable.measurementType);
        });

      }
    };
  };

  return dependencies.concat(resultsTableDirective);
});
