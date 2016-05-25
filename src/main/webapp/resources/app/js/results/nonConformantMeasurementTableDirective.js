'use strict';
define([], function() {
  var dependencies = ['$q', 'ResultsTableService', 'ResultsService', 'NonConformantMeasurementTableService'];

  var nonConformantMeasurementTableDirective = function($q, ResultsTableService, ResultsService, NonConformantMeasurementTableService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/results/nonConformantMeasurementTableDirective.html',
      scope: {
        variable: '=',
        arms: '=',
        groups: '=',
        measurementMoments: '='
      },
      link: function(scope) {

        scope.results = ResultsService.queryNonConformantMeasurements(scope.variable.uri);

        $q.all([scope.arms, scope.measurementMoments, scope.groups, scope.results]).then(function() {
          scope.inputRows = NonConformantMeasurementTableService.createInputRows(scope.variable, scope.arms, scope.groups, scope.results.$$state.value);
          scope.inputHeaders = ResultsTableService.createHeaders(scope.variable.measurementType);
        });

        scope.setToMoment = function(moment, measurementInstanceList) {
          ResultsService.setToMeasurementMoment(moment.uri, measurementInstanceList).then(function(){
            scope.$emit('refreshResults');
            return;
          });
        };

        scope.updateIsExistingMeasurement = function(moment, measurementInstanceList) {
          ResultsService.isExistingMeasurement(moment.uri, measurementInstanceList).then(function(result){
            moment.isExistingMeasurement = result;
          });
        };

      }
    };
  };

  return dependencies.concat(nonConformantMeasurementTableDirective);
});
