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
        measurementMoments: '='
      },
      link: function(scope) {

        var queryResultsDefer = $q.defer();
        ResultsService.queryResults(scope.variable.uri).then(function(result) {
          scope.results = results;
          queryResultsDefer.resolve();
        });

        $q.all([scope.arms, scope.measurementMoments, queryResultsDefer.promise]).then(function() {
          scope.inputRows = ResultsTableService.createInputRows(scope.variable, scope.arms, scope.measurementMoments, scope.results);
          scope.inputHeaders = ResultsTableService.createHeaders(scope.variable.measurementType);
        });

        scope.updateValue = function(row, column) {
          ResultsService.updateResultValue(row, column);
        }
      }
    };
  };

  return dependencies.concat(resultsTableDirective);
});
