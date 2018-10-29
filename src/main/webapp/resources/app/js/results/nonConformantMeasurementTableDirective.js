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
        // functions 
        scope.toggle = toggle;
        scope.setToMoment = setToMoment;
        scope.updateIsExistingMeasurement = updateIsExistingMeasurement;
        scope.repairNonConformantMeasurements = repairNonConformantMeasurements;

        // init
        scope.isExpanded = false;
        reloadData();
        scope.$on('refreshResultsTable', reloadData);

        function toggle() {
          if (scope.isExpanded) {
            scope.isExpanded = false;
          } else {
            reloadData();
            scope.isExpanded = true;
          }
        }

        function setToMoment(moment, measurementInstanceList) {
          ResultsService.setToMeasurementMoment(moment.uri, measurementInstanceList).then(function() {
            scope.$emit('refreshResults');
          });
        }

        function updateIsExistingMeasurement(moment, measurementInstanceList) {
          ResultsService.isExistingMeasurement(moment.uri, measurementInstanceList).then(function(result) {
            moment.isExistingMeasurement = result;
          });
        }

        function repairNonConformantMeasurements() {
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
        }

        function reloadData() {
          scope.nonConformantMeasurementsPromise = ResultsService.queryNonConformantMeasurementsByOutcomeUri(scope.variable.uri);

          $q.all([scope.arms, scope.measurementMoments, scope.groups, scope.nonConformantMeasurementsPromise]).then(function(result) {
            scope.nonConformantMeasurements = result[3];
            scope.nonConformantMeasurementsMap = NonConformantMeasurementTableService.mapResultsByLabelAndGroup(scope.arms, scope.groups, scope.nonConformantMeasurements);
            scope.inputRows = NonConformantMeasurementTableService.createInputRows(scope.variable, scope.nonConformantMeasurementsMap);
          });

          scope.inputHeaders = ResultsTableService.createHeaders(scope.variable);
        }
      }
    };
  };

  return dependencies.concat(nonConformantMeasurementTableDirective);
});
