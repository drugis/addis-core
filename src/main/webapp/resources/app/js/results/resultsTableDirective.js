'use strict';
define(['lodash'], function(_) {
  var dependencies = [
    '$q',
    'ResultsTableService',
    'ResultsService'
  ];

  var resultsTableDirective = function(
    $q,
    ResultsTableService,
    ResultsService
  ) {
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
        // functions
        scope.toggle = toggle;
        scope.editMeasurementMoment = editMeasurementMoment;
        scope.checkMeasurementOverlap = checkMeasurementOverlap;
        scope.showEditMeasurementMoment = showEditMeasurementMoment;

        // init
        scope.isExpanded = false;
        scope.$on('refreshResultsTable', reloadResults);

        function reloadResults() {
          if (scope.isExpanded) {
            var resultsPromise = ResultsService.queryResultsByOutcome(scope.variable.uri);

            return $q.all([scope.arms, scope.measurementMoments, scope.groups, resultsPromise]).then(function(values) {
              var arms = values[0];
              var measurementMoments = values[1];
              var groups = values[2];
              var results = values[3];

              scope.inputRows = ResultsTableService.createInputRows(scope.variable, arms, groups, measurementMoments, results);
              scope.inputHeaders = ResultsTableService.createHeaders(scope.variable);
              scope.measurementMomentOptions = ResultsTableService.buildMeasurementMomentOptions(scope.variable.measuredAtMoments);
              scope.measurementMomentSelections = ResultsTableService.buildMeasurementMomentSelections(scope.inputRows, scope.measurementMomentOptions);
              checkMeasurementOverlap();
              scope.isEditingMM = _.reduce(scope.measurementMoments, function(accum, measurementMoment) {
                accum[measurementMoment.uri] = false;
                return accum;
              }, {});
              scope.hasNotAnalysedProperty = ResultsTableService.findNotAnalysedProperty(scope.inputHeaders);
            });
          }
        }

        function show() {
          scope.isExpanded = true;
          reloadResults();
        }

        function hide() {
          scope.isExpanded = false;
          delete scope.results;
        }

        function toggle() {
          if (scope.isExpanded) {
            hide();
          } else {
            show();
          }
        }

        function editMeasurementMoment(measurementMomentUri, rowLabel) {
          ResultsService.moveMeasurementMoment(measurementMomentUri,
            scope.measurementMomentSelections[measurementMomentUri].uri,
            scope.variable.uri, rowLabel
          ).then(function() {
            scope.$emit('refreshResults');
            scope.isEditingMM[measurementMomentUri] = false;
          });
        }

        function checkMeasurementOverlap() {
          scope.overlapMap = ResultsTableService.findMeasurementOverlap(scope.measurementMomentSelections, scope.inputRows);
        }

        function showEditMeasurementMoment(measurementMomentUri) {
          reloadResults().then(function() {
            scope.isEditingMM[measurementMomentUri] = true;
          });
        }
      }
    };
  };

  return dependencies.concat(resultsTableDirective);
});
