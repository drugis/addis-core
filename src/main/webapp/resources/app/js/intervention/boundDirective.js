'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$stateParams', 'InterventionService', 'MappingService'];
  var boundDirective = function($stateParams, InterventionService, MappingService) {
    return {
      scope: {
        boundType: '=',
        bound: '=',
        scaledUnits: '='
      },
      link: function(scope) {
        // vars
        if (scope.boundType === 'lower') {
          scope.boundName = 'Lower';
          scope.BOUND_OPTIONS = InterventionService.LOWER_BOUND_OPTIONS;
        } else {
          scope.boundName = 'Upper';
          scope.BOUND_OPTIONS = InterventionService.UPPER_BOUND_OPTIONS;
        }
        scope.metricMultipliers = MappingService.METRIC_MULTIPLIERS;
        scope.editPeriod = {};

        //functions
        scope.toggleBound = toggleBound;
        scope.updatePeriodicity = updatePeriodicity;

        function toggleBound(newState) {
          if (!newState) {
            delete scope.bound;
          } else {
            _.extend(scope.bound, {
              type: scope.BOUND_OPTIONS[0],
              unit: scope.scaledUnits[0],
              unitPeriod: 'P1D'
            });
          }
        }

        function updatePeriodicity() {
          var duration = {
            periodType: scope.lowerBoundPeriodicity.unit,
            numberOfPeriods: scope.lowerBoundPeriodicity.amount
          };
          scope.model.lowerBound.unit.unitPeriod = DurationService.generateDurationString(duration);
          if (!scope.model.lowerBound.unit.unitPeriod) {
            scope.model.lowerBound.unit.unitPeriod = 'P1D';
          }
        }

        function updatePeriod() {
          if (!scope.model.lowerBound.unit.unitPeriod) {
            scope.model.lowerBound.unit.unitPeriod = 'P1D';
          } else {
            updateLowerBoundPeriodicity();
          }
        }
      },
      restrict: 'E',
      templateUrl: '/app/js/intervention/boundDirective.html'
    };
  };

  return dependencies.concat(boundDirective);
});
