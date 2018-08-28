'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$q', 'ResultsTableService', 'ResultsService'];

  var resultsTableDirective = function($q, ResultsTableService, ResultsService) {
    return {
      restrict: 'E',
      templateUrl: './resultsTableDirective.html',
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
            var resultsPromise = ResultsService.queryResults(scope.variable.uri);

            return $q.all([scope.arms, scope.measurementMoments, scope.groups, resultsPromise]).then(function(values) {
              var arms = values[0],
               measurementMoments = values[1],
               groups = values[2],
               results = values[3];
              scope.inputRows = ResultsTableService.createInputRows(scope.variable, arms, groups,
                measurementMoments, results);
              scope.inputHeaders = ResultsTableService.createHeaders(scope.variable);
              scope.measurementMomentOptions = ResultsTableService.buildMeasurementMomentOptions(scope.variable.measuredAtMoments);
              scope.measurementMomentSelections = _.reduce(scope.inputRows, function(accum, inputRow) {
                // for every inputRow, take the uri of its measurementMoment. Then get for this measurementMoment the 
                // first in its list of options available when changing the measurementMoment of this row to another
                // measurementMoment. Then store this option based on the uri of the measurement moment
                var measurementMomentUri = inputRow.measurementMoment.uri;
                accum[measurementMomentUri] = scope.measurementMomentOptions[measurementMomentUri][0];
                return accum;
              }, {});
              scope.checkMeasurementOverlap();
              scope.isEditingMM = _.reduce(scope.measurementMoments, function(accum, measurementMoment) {
                accum[measurementMoment.uri] = false;
                return accum;
              }, {});
              scope.hasNotAnalysedProperty = _.find(scope.inputHeaders, function(header) {
                return header.lexiconKey && !header.analysisReady; // only check if not categorical
              });
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

        scope.editMeasurementMoment = function(measurementMomentUri, rowLabel) {
          ResultsService.moveMeasurementMoment(measurementMomentUri,
              scope.measurementMomentSelections[measurementMomentUri].uri,
              scope.variable.uri,
              rowLabel)
            .then(function() {
              scope.$emit('refreshResults');
              scope.isEditingMM[measurementMomentUri] = false;
            });
        };

        scope.checkMeasurementOverlap = function() {
          scope.overlapMap = _.reduce(scope.measurementMomentSelections, function(accum, selection, measurementMomentUri) {
            accum[measurementMomentUri] = ResultsTableService.findOverlappingMeasurements(selection.uri, scope.inputRows);
            return accum;
          }, {});
        };

        scope.showEditMeasurementMoment = function(measurementMomentUri) {
          reloadResults().then(function() {
            scope.isEditingMM[measurementMomentUri] = true;
          });
        };
      }
    };
  };

  return dependencies.concat(resultsTableDirective);
});
