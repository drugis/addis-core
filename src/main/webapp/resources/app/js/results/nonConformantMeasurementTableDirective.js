'use strict';
define([], function() {
  var dependencies = ['$q', '$modal', 'ResultsTableService', 'ResultsService', 'NonConformantMeasurementTableService'];

  var nonConformantMeasurementTableDirective = function($q, $modal, ResultsTableService, ResultsService, NonConformantMeasurementTableService) {
    return {
      restrict: 'E',
      templateUrl: './nonConformantMeasurementTableDirective.html',
      scope: {
        variableType: '=',
        variable: '=',
        arms: '=',
        groups: '=',
        measurementMoments: '=',
        isEditingAllowed: '='
      },
      link: function(scope) {
        scope.isExpanded = false;

        reloadData();
        scope.toggle = function() {
          if (scope.isExpanded) {
            scope.isExpanded = false;
          } else {
            reloadData();
            scope.isExpanded = true;
          }
        };

        scope.$on('refreshResultsTable', reloadData);

        function reloadData() {
          scope.nonConformantMeasurementsPromise = ResultsService.queryNonConformantMeasurementsByOutcomeUri(scope.variable.uri);

          $q.all([scope.arms, scope.measurementMoments, scope.groups, scope.nonConformantMeasurementsPromise]).then(function(result) {
            scope.nonConformantMeasurements = result[3];
            scope.nonConformantMeasurementsMap = NonConformantMeasurementTableService.mapResultsByLabelAndGroup(scope.arms, scope.groups, scope.nonConformantMeasurements);
            scope.inputRows = NonConformantMeasurementTableService.createInputRows(scope.variable, scope.nonConformantMeasurementsMap);
          });

          scope.inputHeaders = ResultsTableService.createHeaders(scope.variable);

          scope.setToMoment = function(moment, measurementInstanceList) {
            ResultsService.setToMeasurementMoment(moment.uri, measurementInstanceList).then(function() {
              scope.$emit('refreshResults');
            });
          };

          scope.updateIsExistingMeasurement = function(moment, measurementInstanceList) {
            ResultsService.isExistingMeasurement(moment.uri, measurementInstanceList).then(function(result) {
              moment.isExistingMeasurement = result;
            });
          };

          scope.repairNonConformantMeasurements = function() {
            $modal.open({
              templateUrl: './splitOutcomeTemplate.html',
              controller: 'SplitOutcomeController',
              resolve: {
                callback: function() {
                  return function() {
                    scope.$emit('updateStudyDesign');
                  };
                },
                outcome: function() {
                  return scope.variable;
                },
                nonConformantMeasurementsMap: function() {
                  return scope.nonConformantMeasurementsMap;
                },
                variableType: function() {
                  return scope.variableType;
                }
              }
            });
          };
        }



      }
    };
  };

  return dependencies.concat(nonConformantMeasurementTableDirective);
});
