'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$q', 'ResultsTableService', 'ResultsService'];

  var resultsTableDirective = function($q, ResultsTableService, ResultsService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/results/resultsTableDirective.html',
      scope: {
        variable: '=',
        arms: '=',
        groups: '=',
        measurementMoments: '=',
        isEditingAllowed: '='
      },
      link: function(scope) {
        function reloadResults() {
          if (scope.isExpanded) {
            scope.results = ResultsService.queryResults(scope.variable.uri);

            $q.all([scope.arms, scope.measurementMoments, scope.groups, scope.results]).then(function() {
              scope.inputRows = ResultsTableService.createInputRows(scope.variable, scope.arms, scope.groups, scope.measurementMoments, scope.results.$$state.value);
              scope.inputHeaders = ResultsTableService.createHeaders(scope.variable);
              scope.measurementMomentOptions = ResultsTableService.buildMeasurementMomentOptions(scope.measurementMoments);
              scope.measurementMomentSelections = _.reduce(scope.inputRows, function(accum, inputRow) {
                var measurementMomentUri = inputRow.measurementMoment.uri;
                accum[measurementMomentUri] = scope.measurementMomentOptions[measurementMomentUri][0];
                return accum;
              }, {});
            });
          }
        }

        scope.$on('refreshResultsTable', reloadResults);

        scope.isExpanded = false;


        scope.show = function() {
          scope.isExpanded = true;
          reloadResults();
        };

        scope.hide = function() {
          scope.isExpanded = false;
          delete scope.results;
        };

        scope.toggle = function() {
          if (scope.isExpanded) {
            scope.hide();
          } else {
            scope.show();
          }
        };

        scope.editMeasurementMoment = function(measurementMomentUri) {
        };

      }
    };
  };

  return dependencies.concat(resultsTableDirective);
});
