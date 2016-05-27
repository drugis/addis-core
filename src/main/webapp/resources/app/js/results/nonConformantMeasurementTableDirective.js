'use strict';
define([], function() {
  var dependencies = ['$q', '$modal', 'ResultsTableService', 'ResultsService', 'NonConformantMeasurementTableService'];

  var nonConformantMeasurementTableDirective = function($q, $modal, ResultsTableService, ResultsService, NonConformantMeasurementTableService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/results/nonConformantMeasurementTableDirective.html',
      scope: {
        variableType: '=',
        variable: '=',
        arms: '=',
        groups: '=',
        measurementMoments: '='
      },
      link: function(scope) {

        scope.nonConformantMeasurementsPromise = ResultsService.queryNonConformantMeasurements(scope.variable.uri);

        $q.all([scope.arms, scope.measurementMoments, scope.groups, scope.nonConformantMeasurementsPromise]).then(function(result) {
          scope.nonConformantMeasurements = result[3];
          scope.nonConformantMeasurementsMap = NonConformantMeasurementTableService.mapResultsByLabelAndGroup(scope.arms, scope.groups, scope.nonConformantMeasurements);
          scope.inputRows = NonConformantMeasurementTableService.createInputRows(scope.variable, scope.nonConformantMeasurementsMap);
          scope.inputHeaders = ResultsTableService.createHeaders(scope.variable.measurementType);

          scope.setToMoment = function(moment, measurementInstanceList) {
            ResultsService.setToMeasurementMoment(moment.uri, measurementInstanceList).then(function() {
              scope.$emit('refreshResults');
              return;
            });
          };

          scope.updateIsExistingMeasurement = function(moment, measurementInstanceList) {
            ResultsService.isExistingMeasurement(moment.uri, measurementInstanceList).then(function(result) {
              moment.isExistingMeasurement = result;
            });
          };

          scope.repairNonConformantMeasurements = function() {
            $modal.open({
              templateUrl: 'app/js/results/splitOutcomeTemplate.html',
              controller: 'SplitOutcomeController',
              resolve: {
                callback: function() {
                  return function(){
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

        });



      }
    };
  };

  return dependencies.concat(nonConformantMeasurementTableDirective);
});
